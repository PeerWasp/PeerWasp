#include "ContextMenuExt.h"
#include <strsafe.h>
#include <Shlwapi.h>
#pragma comment(lib, "shlwapi.lib")

#include <cpprest/http_client.h>				// HTTP and Uri
#include <cpprest/json.h>                       // JSON LIBRARY

#include "Command.h"
#include "Utils.h"
#include "IconHelper.h"

extern HINSTANCE g_hInst;
extern long g_cDllRef;


std::map<CommandId, CommandInfo> commandInfoMap = Command::CreateCommandMap();


ContextMenuExt::ContextMenuExt(void) : m_cRef(1),
	/* selection flags */
	m_selectionOnlyOne(false),
	m_selectionHasFolders(false),
	m_selectionHasFiles(false),
	/* menu handles*/
	m_hTopMenu(NULL),
	m_hSubMenu(NULL),
	/* indices */
	m_firstCmdId(0),
	m_currentCmdId(0),
	m_topMenuIndex(0),
	m_subMenuIndex(0)
{
    InterlockedIncrement(&g_cDllRef);

}

ContextMenuExt::~ContextMenuExt(void)
{
	// free all icon bitmaps
	std::map<CommandId, HBITMAP>::iterator it = m_icons.begin();
	while (it != m_icons.end()) {
		DeleteObject(it->second);
		++it;
	}

	m_icons.clear();
	m_files.clear();
	m_cmdIdToCommand.clear();

    InterlockedDecrement(&g_cDllRef);
}

void ContextMenuExt::UpdateSelectionFlags(std::wstring lastPath)
{
	// ONE: number of files
	m_selectionOnlyOne = (m_files.size() == 1);

	// FOLDER: if folder flag not set, check whether last path is a folder
	bool isFolder = PathIsDirectoryW(lastPath.c_str()) != false;
	if (!m_selectionHasFolders && isFolder){
		m_selectionHasFolders = true;
	}

	// FILE: if file flag not set, check whether last path is a file
	if (!m_selectionHasFiles && !isFolder) {
		m_selectionHasFiles = true;
	}
}

#pragma region IUnknown

// Query to the interface the component supported.
IFACEMETHODIMP ContextMenuExt::QueryInterface(REFIID riid, void **ppv)
{
    static const QITAB qit[] = 
    {
        QITABENT(ContextMenuExt, IContextMenu),
        QITABENT(ContextMenuExt, IShellExtInit), 
        { 0 },
    };
    return QISearch(this, qit, riid, ppv);
}

// Increase the reference count for an interface on an object.
IFACEMETHODIMP_(ULONG) ContextMenuExt::AddRef()
{
    return InterlockedIncrement(&m_cRef);
}

// Decrease the reference count for an interface on an object.
IFACEMETHODIMP_(ULONG) ContextMenuExt::Release()
{
    ULONG cRef = InterlockedDecrement(&m_cRef);
    if (0 == cRef)
    {
        delete this;
    }

    return cRef;
}

#pragma endregion


#pragma region IShellExtInit

// Initialize the context menu handler.
IFACEMETHODIMP ContextMenuExt::Initialize(
    LPCITEMIDLIST pidlFolder, LPDATAOBJECT pDataObj, HKEY hKeyProgID)
{

	m_selectionHasFiles = false;
	m_selectionHasFolders = false;
	m_selectionOnlyOne = false;
	m_files.clear();

    if (NULL == pDataObj)
    {
        return E_INVALIDARG;
    }

    HRESULT hr = E_FAIL;

    FORMATETC fe = { CF_HDROP, NULL, DVASPECT_CONTENT, -1, TYMED_HGLOBAL };
    STGMEDIUM stm;

    // The pDataObj pointer contains the objects being acted upon. In this 
    // example, we get an HDROP handle for enumerating the selected files and 
    // folders.
    if (SUCCEEDED(pDataObj->GetData(&fe, &stm)))
    {
        // Get an HDROP handle.
        HDROP hDrop = static_cast<HDROP>(GlobalLock(stm.hGlobal));
        if (hDrop != NULL)
        {
            // Determine how many files are involved in this operation.
            UINT nFiles = DragQueryFile(hDrop, 0xFFFFFFFF, NULL, 0);
			// Fill files vector with paths
			for (UINT i = 0; i < nFiles; ++i) {
				wchar_t tmpPath[MAX_PATH + 1] = {'\0'};
				if (0 != DragQueryFile(hDrop, i, tmpPath, ARRAYSIZE(tmpPath)))
				{
					std::wstring currentPath(tmpPath);
					bool isInRoot = Utils::IsInsideRootPath(currentPath);
					if (isInRoot) {
						m_files.push_back(currentPath);
						UpdateSelectionFlags(currentPath);
						hr = S_OK;
					}
					else {
						// if at leat one selected file is not in the root path, 
						// we do not show the menu
						hr = E_INVALIDARG;
						break;
					}
				}
			}

            GlobalUnlock(stm.hGlobal);
        }

        ReleaseStgMedium(&stm);
    }

    // If any value other than S_OK is returned from the method, the context 
    // menu item is not displayed.
    return hr;
}

#pragma endregion


#pragma region IContextMenu

HRESULT ContextMenuExt::LoadAndSetBitmapByIcon(MENUITEMINFO *mii, CommandId cmd)
{
	HRESULT hr = E_FAIL;
	HBITMAP bitmap = NULL;

	// check preconditions
	if (mii == NULL) {
		return hr;
	}
	// command info exists?
	if (commandInfoMap.find(cmd) == commandInfoMap.end()) {
		return hr;
	}
	// check whether there is an icon for that cmd
	if (!Command::hasIcon(&commandInfoMap[cmd])) {
		return hr;
	}

	// load the icon into bitmap
	hr = IconHelper::LoadBitmapByIcon(commandInfoMap[cmd].bitmapResourceId, &bitmap);

	// if loading succeeded, set bitmap to menu item
	if (SUCCEEDED(hr) && (bitmap != NULL)) {
		mii->fMask |= MIIM_BITMAP;		/* bitmap icon flag: icon in hbmpItem member */
		mii->hbmpItem = bitmap;
		m_icons[cmd] = bitmap; // store, to free later
		hr = S_OK;
	}

	return hr;
}

bool ContextMenuExt::CreateTopMenu()
{
	// Add a separator.
	if (!MenuItemInitSeparator(m_hTopMenu, m_topMenuIndex)) {
		return false;
	}
	++m_topMenuIndex;

	// Top level menu
	if (!MenuItemInitTop(m_topMenuIndex, m_currentCmdId)) {
		return false;
	}
	++m_topMenuIndex;
	++m_currentCmdId;

	if (!MenuItemInitSeparator(m_hTopMenu, m_topMenuIndex)) {
		return false;
	}
	++m_topMenuIndex;

	return true;
}

bool ContextMenuExt::MenuItemInitTop(UINT pos, UINT cmdId)
{
	MENUITEMINFO mii = { sizeof(mii) };
	mii.fMask = MIIM_STRING		/* use of dwTypeData member */
		| MIIM_FTYPE			/* use of fType member */
		| MIIM_ID				/* use of wID member */
		| MIIM_STATE			/* use of fState member */
		| MIIM_SUBMENU;			/* submenu: hSubMenu member*/
	mii.fType = MFT_STRING;		/* dwTypeData is the actual string */
	mii.fState = MFS_ENABLED;	
	mii.wID = cmdId;
	mii.hSubMenu = m_hSubMenu;
	mii.dwTypeData = &commandInfoMap[CMD_TOP].menuText[0];
	LoadAndSetBitmapByIcon(&mii, CMD_TOP);

	if (!InsertMenuItem(m_hTopMenu, pos, TRUE, &mii))
	{
		return false;
	}
	m_cmdIdToCommand[cmdId] = CommandId::CMD_TOP;
	return true;
}

bool ContextMenuExt::CreateSubMenu()
{
	m_hSubMenu = CreatePopupMenu();

	if (!MenuItemInitDelete(m_subMenuIndex, m_currentCmdId)) {
		return false;
	}
	++m_subMenuIndex;
	++m_currentCmdId;

	if (!MenuItemInitVersions(m_subMenuIndex, m_currentCmdId)) {
		return false;
	}
	++m_subMenuIndex;
	++m_currentCmdId;

	if (!MenuItemInitShare(m_subMenuIndex, m_currentCmdId)) {
		return false;
	}
	++m_subMenuIndex;
	++m_currentCmdId;

	return true;
}

bool ContextMenuExt::MenuItemInitSeparator(HMENU menuHandle, UINT pos)
{
	MENUITEMINFO mii = { sizeof(mii) };
	mii.fMask = MIIM_TYPE;
	mii.fType = MFT_SEPARATOR;

	if (!InsertMenuItem(menuHandle, pos, TRUE, &mii))
	{
		return false;
	}
	return true;
}

bool ContextMenuExt::MenuItemInitDelete(UINT pos, UINT cmdId)
{
	MENUITEMINFO mii = { sizeof(mii) };
	mii.fMask = MIIM_STRING | MIIM_FTYPE | MIIM_ID | MIIM_STATE;
	mii.fType = MFT_STRING;
	mii.fState = MFS_GRAYED;
	// need folders or files
	if (m_selectionHasFiles || m_selectionHasFolders) {
		mii.fState = MFS_ENABLED;
	}
	mii.wID = cmdId;
	mii.dwTypeData = &commandInfoMap[CMD_DELETE].menuText[0];
	LoadAndSetBitmapByIcon(&mii, CMD_DELETE);

	if (!InsertMenuItem(m_hSubMenu, pos, TRUE, &mii))
	{
		return false;
	}
	m_cmdIdToCommand[cmdId] = CommandId::CMD_DELETE;
	return true;
}

bool ContextMenuExt::MenuItemInitVersions(UINT pos, UINT cmdId)
{
	MENUITEMINFO mii = { sizeof(mii) };
	mii.fMask = MIIM_STRING | MIIM_FTYPE | MIIM_ID | MIIM_STATE;
	mii.fType = MFT_STRING;
	mii.fState = MFS_GRAYED;
	// enable only for single file
	if (m_selectionOnlyOne && m_selectionHasFiles && !m_selectionHasFolders) {
		mii.fState = MFS_ENABLED;
	}
	mii.wID = cmdId;
	mii.dwTypeData = &commandInfoMap[CMD_VERSIONS].menuText[0];
	LoadAndSetBitmapByIcon(&mii, CMD_VERSIONS);

	if (!InsertMenuItem(m_hSubMenu, pos, TRUE, &mii))
	{
		return false;
	}
	m_cmdIdToCommand[cmdId] = CommandId::CMD_VERSIONS;
	return true;
}

bool ContextMenuExt::MenuItemInitShare(UINT pos, UINT cmdId)
{
	MENUITEMINFO mii = { sizeof(mii) };
	mii.fMask = MIIM_STRING | MIIM_FTYPE | MIIM_ID | MIIM_STATE;
	mii.fType = MFT_STRING;
	mii.fState = MFS_GRAYED;
	// enable only for single folder
	if (m_selectionOnlyOne && m_selectionHasFolders && !m_selectionHasFiles) {
		mii.fState = MFS_ENABLED;
	}
	mii.wID = cmdId;
	mii.dwTypeData = &commandInfoMap[CMD_SHARE].menuText[0];
	LoadAndSetBitmapByIcon(&mii, CMD_SHARE);

	if (!InsertMenuItem(m_hSubMenu, pos, TRUE, &mii))
	{
		return false;
	}
	m_cmdIdToCommand[cmdId] = CommandId::CMD_SHARE;
	return true;
}

//
//   FUNCTION: ContextMenuExt::QueryContextMenu
//
//   PURPOSE: The Shell calls IContextMenu::QueryContextMenu to allow the 
//            context menu handler to add its menu items to the menu. It 
//            passes in the HMENU handle in the hmenu parameter. The 
//            indexMenu parameter is set to the index to be used for the 
//            first menu item that is to be added.
//
// doc about MENUITEMINFO: 
// http://msdn.microsoft.com/en-us/library/windows/desktop/ms647578(v=vs.85).aspx
//
// doc about InsertMenuItem:
// http://msdn.microsoft.com/en-us/library/windows/desktop/ms647988(v=vs.85).aspx
//
IFACEMETHODIMP ContextMenuExt::QueryContextMenu(
    HMENU hMenu, UINT indexMenu, UINT idCmdFirst, UINT idCmdLast, UINT uFlags)
{
    // If uFlags include CMF_DEFAULTONLY then we should not do anything.
    if (CMF_DEFAULTONLY & uFlags)
    {
        return MAKE_HRESULT(SEVERITY_SUCCESS, 0, USHORT(0));
    }

	// context menu menu handle
	m_hTopMenu = hMenu;
	// position index
	m_topMenuIndex = indexMenu;
	m_subMenuIndex = 0;
	// command id
	m_firstCmdId = idCmdFirst;
	m_currentCmdId = idCmdFirst;

	m_cmdIdToCommand.clear();

	// create and populate the submenu.
	if (!CreateSubMenu()) {
		return HRESULT_FROM_WIN32(GetLastError());
	}

	// create and populate top level menu
	if (!CreateTopMenu()) {
		return HRESULT_FROM_WIN32(GetLastError());
	}
	
    // Return an HRESULT value with the severity set to SEVERITY_SUCCESS. 
    // Set the code value to the offset of the largest command identifier 
    // that was assigned, plus one (1).
	return MAKE_HRESULT(SEVERITY_SUCCESS, 0, USHORT(m_currentCmdId - idCmdFirst + 1));
}


//
//   FUNCTION: ContextMenuExt::InvokeCommand
//
//   PURPOSE: This method is called when a user clicks a menu item to tell 
//            the handler to run the associated command. The lpcmi parameter 
//            points to a structure that contains the needed information.
// doc: http://msdn.microsoft.com/en-us/library/windows/desktop/bb776096(v=vs.85).aspx
// 
IFACEMETHODIMP ContextMenuExt::InvokeCommand(LPCMINVOKECOMMANDINFO pici)
{

	// !! NOTE: we do not support invocation by verb name !!
	// only by the commad id (offset)


    BOOL fUnicode = FALSE;

    // Determine which structure is being passed in, CMINVOKECOMMANDINFO or 
    // CMINVOKECOMMANDINFOEX based on the cbSize member of lpcmi. Although 
    // the lpcmi parameter is declared in Shlobj.h as a CMINVOKECOMMANDINFO 
    // structure, in practice it often points to a CMINVOKECOMMANDINFOEX 
    // structure. This struct is an extended version of CMINVOKECOMMANDINFO 
    // and has additional members that allow Unicode strings to be passed.
    if (pici->cbSize == sizeof(CMINVOKECOMMANDINFOEX))
    {
        if (pici->fMask & CMIC_MASK_UNICODE)
        {
            fUnicode = TRUE;
        }
    }

    // Determines whether the command is identified by its offset or verb.
    // There are two ways to identify commands:
    // 
    //   1) The command's verb string 
    //   2) The command's identifier offset
    // 
    // If the high-order word of lpcmi->lpVerb (for the ANSI case) or 
    // lpcmi->lpVerbW (for the Unicode case) is nonzero, lpVerb or lpVerbW 
    // holds a verb string. If the high-order word is zero, the command 
    // offset is in the low-order word of lpcmi->lpVerb.

    // For the ANSI case, if the high-order word is not zero, the command's 
    // verb string is in lpcmi->lpVerb. 
    if (!fUnicode && HIWORD(pici->lpVerb))
    {
		//      // Is the verb supported by this context menu extension?
		//	  if (StrCmpIA(pici->lpVerb, ##SOME STRING##) == 0)
		//      {
		//          executecommand(pici->hwnd);
		//      }

		// !! NOTE: we do not support invocation by verb name !!

		// If the verb is not recognized by the context menu handler, it 
		// must return E_FAIL to allow it to be passed on to the other 
		// context menu handlers that might implement that verb.
		return E_FAIL;
    }

    // For the Unicode case, if the high-order word is not zero, the 
    // command's verb string is in lpcmi->lpVerbW. 
    else if (fUnicode && HIWORD(((CMINVOKECOMMANDINFOEX*)pici)->lpVerbW))
    {
	  //      // Is the verb supported by this context menu extension?
	  //	  if (StrCmpIW(((CMINVOKECOMMANDINFOEX*)pici)->lpVerbW, ##SOME STRING##) == 0)
	  //      {
	  //			executecommand(pici->hwnd);
	  //      }

		// !! NOTE: we do not support invocation by verb name !!

		// If the verb is not recognized by the context menu handler, it 
		// must return E_FAIL to allow it to be passed on to the other 
		// context menu handlers that might implement that verb.
		return E_FAIL;
    }

    // If the command cannot be identified through the verb string, then 
    // check the identifier offset.
    else
    {
        // Is the command identifier offset supported by this context menu extension?
		UINT cmdOffset = LOWORD(pici->lpVerb);
		UINT cmdId = m_firstCmdId + cmdOffset;
		std::map<UINT, CommandId>::iterator it = m_cmdIdToCommand.find(cmdId);

		if (it != m_cmdIdToCommand.end() && it->first == cmdId) {
			CommandId cmdId = it->second;
			switch (cmdId) {
			case CMD_DELETE:
				Handle_CmdDelete();
				break;
			case CMD_VERSIONS:
				Handle_CmdVersions();
				break;
			case CMD_SHARE:
				Handle_CmdShare();
				break;
			default:
				// unknown command id -- should not happen
				return E_FAIL;
			}
		}
		else {
			// If the verb is not recognized by the context menu handler, it 
			// must return E_FAIL to allow it to be passed on to the other 
			// context menu handlers that might implement that verb.
			return E_FAIL;
		}
    }

    return S_OK;
}


//
//   FUNCTION: ContextMenuExt::GetCommandString
//
//   PURPOSE: If a user highlights one of the items added by a context menu 
//            handler, the handler's IContextMenu::GetCommandString method is 
//            called to request a Help text string that will be displayed on 
//            the Windows Explorer status bar. This method can also be called 
//            to request the verb string that is assigned to a command. 
//            Either ANSI or Unicode verb strings can be requested. This 
//            example only implements support for the Unicode values of 
//            uFlags, because only those have been used in Windows Explorer 
//            since Windows 2000.
//
IFACEMETHODIMP ContextMenuExt::GetCommandString(UINT_PTR idCommand, 
    UINT uFlags, UINT *pwReserved, LPSTR pszName, UINT cchMax)
{
    HRESULT hr = E_INVALIDARG;
	
    switch (uFlags)
    {
	case GCS_HELPTEXTW: {
		std::wstring helpTxt = GetHelpText(idCommand);
		hr = StringCchCopy(reinterpret_cast<PWSTR>(pszName), cchMax, &helpTxt[0]);
		break;
	}

	case GCS_VERBW: {
		// NOT SUPPORTED
		// GCS_VERBW is an optional feature that enables a caller to 
		// discover the canonical name for the verb passed in through 
		// idCommand.
		// hr = StringCchCopy(reinterpret_cast<PWSTR>(pszName), cchMax, ##some string##);
		hr = E_INVALIDARG;
		break;
	}

    default:
        hr = E_INVALIDARG;
    }
 

    // If the command (idCommand) is not supported by this context menu 
    // extension handler, return E_INVALIDARG.

    return hr;
}

std::wstring ContextMenuExt::GetHelpText(UINT cmdOffset) 
{
	// perform two lookups
	// 1. lookup the Command
	// 2. lookup the CommandInfo
	UINT commandId = m_firstCmdId + cmdOffset;
	std::map<UINT, CommandId>::iterator it = m_cmdIdToCommand.find(commandId);
	if (it != m_cmdIdToCommand.end() && it->first == commandId) {
		CommandId cmd = it->second;
		std::map<CommandId, CommandInfo>::iterator info = commandInfoMap.find(cmd);
		if (info != commandInfoMap.end() && info->first == cmd) {
			return info->second.helpText;
		}
	}

	// return empty help string if no help text found
	return L""; 
}

#pragma endregion


#pragma region CommandHandlers

//
// doc: http://msdn.microsoft.com/en-us/library/jj988008.aspx
//

void ContextMenuExt::Handle_CmdDelete()
{
	Utils::GetApiServerPort();
	// check preconditions
	if (m_files.size() < 1) {
		return;
	}

	try {

		// collect paths
		std::vector<web::json::value> jsonPaths;
		std::vector<std::wstring>::const_iterator it;
		for (it = m_files.cbegin(); it < m_files.cend(); ++it) {
			jsonPaths.push_back(web::json::value::string(*it));
		}

		// setup message
		web::json::value data;
		data[L"command"] = web::json::value::string(L"Delete");
		data[L"paths"] = web::json::value::array(jsonPaths);

		// post message
		web::http::uri uri = Utils::CreateUri(L"delete");
		web::http::client::http_client client(uri);
		pplx::task<web::http::http_response> task = client.request(web::http::methods::POST, L"", data.serialize(), L"application/json");
		task.wait();
		web::http::http_response response = task.get();
		if (response.status_code() == web::http::status_codes::OK)
		{
			// everything ok
		}
		else {
			// log somehow? show message box?
		}
	}
	catch (...) {

	}
}

void ContextMenuExt::Handle_CmdVersions()
{
	// check preconditions
	if (m_files.size() != 1) {
		return;
	}

	try {

		// setup message
		web::json::value data;
		data[L"command"] = web::json::value::string(L"Versions");
		data[L"path"] = web::json::value::string(m_files.at(0));

		// post message
		web::http::uri uri = Utils::CreateUri(L"versions");
		web::http::client::http_client client(uri);
		pplx::task<web::http::http_response> task = client.request(web::http::methods::POST, L"", data.serialize(), L"application/json");
		task.wait();
		web::http::http_response response = task.get();
		if (response.status_code() == web::http::status_codes::OK)
		{
			// everything ok
		}
		else {
			// log somehow? show message box?
		}
	}
	catch (...) {

	}
}


void ContextMenuExt::Handle_CmdShare()
{
	// check preconditions
	if (m_files.size() != 1) {
		return;
	}

	try {

		// setup message
		web::json::value data;
		data[L"command"] = web::json::value::string(L"ShareFolder");
		data[L"path"] = web::json::value::string(m_files.at(0));

		// post message
		web::http::uri uri = Utils::CreateUri(L"share");
		web::http::client::http_client client(uri);
		pplx::task<web::http::http_response> task = client.request(web::http::methods::POST, L"", data.serialize(), L"application/json");
		task.wait();
		web::http::http_response response = task.get();
		if (response.status_code() == web::http::status_codes::OK)
		{
			// everything ok
		}
		else {
			// log somehow? show message box?
		}
	}
	catch (...) {

	}
}

#pragma endregion