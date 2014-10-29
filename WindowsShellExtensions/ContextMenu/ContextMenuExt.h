#pragma once

#include <windows.h>
#include <shlobj.h>     // For IShellExtInit and IContextMenu

#include <vector>
#include <map>


#include "Utils.h"
#include "Command.h"

class ContextMenuExt : public IShellExtInit, public IContextMenu
{
public:
    // IUnknown
    IFACEMETHODIMP QueryInterface(REFIID riid, void **ppv);
    IFACEMETHODIMP_(ULONG) AddRef();
    IFACEMETHODIMP_(ULONG) Release();

    // IShellExtInit
    IFACEMETHODIMP Initialize(LPCITEMIDLIST pidlFolder, LPDATAOBJECT pDataObj, HKEY hKeyProgID);

    // IContextMenu
    IFACEMETHODIMP QueryContextMenu(HMENU hMenu, UINT indexMenu, UINT idCmdFirst, UINT idCmdLast, UINT uFlags);
    IFACEMETHODIMP InvokeCommand(LPCMINVOKECOMMANDINFO pici);
    IFACEMETHODIMP GetCommandString(UINT_PTR idCommand, UINT uFlags, UINT *pwReserved, LPSTR pszName, UINT cchMax);
	
    ContextMenuExt(void);

protected:
    ~ContextMenuExt(void);

private:
    // Reference count of component.
    long m_cRef;

	// menu handle of top menu, given by QueryContextMenu (hMenu parameter)
	HMENU m_hTopMenu;

	// submenu handle, created in QueryContextMenu 
	HMENU m_hSubMenu;

	// the first command id as given by QueryContextMenu (idCmdFirst parameter)
	// denotes the first command id to use
	UINT m_firstCmdId;

	// the next command id to use (increased with each menu items that is added)
	UINT m_currentCmdId;

	// position index of the top context menu, initially given by QueryContextMenu (indexMenu parameter)
	UINT m_topMenuIndex;

	// next position index of the submenu (increased with each menu item)
	UINT m_subMenuIndex;

	// Bitmap handles for the menu icons
	// will be free'd in deconstructor
	std::map<CommandId, HBITMAP> m_icons;

	// maps cmdIds assigned to menu items to the internal command enum
	std::map<UINT, CommandId> m_cmdIdToCommand;

	// files and folders that are selected, populated by Initialize
	std::vector<std::wstring> m_files;

	// following flags indicate type of the selected files:

	// true if at least 1 file in selection
	bool m_selectionHasFiles;

	// true if at least 1 folder in selection
	bool m_selectionHasFolders;

	// true if exactly 1 item selected
	bool m_selectionOnlyOne;


	// load and set icon for a menu itme given a command
	HRESULT LoadAndSetBitmapByIcon(MENUITEMINFO *mii, CommandId cmd);

	// returns the help text for a command item
	// commandId is the key in the m_cmdIdToCommand map
	std::wstring ContextMenuExt::GetHelpText(UINT commandId);

	// updates the selection flags depending on the current selection of files
	void UpdateSelectionFlags(std::wstring lastPath);

	// menu building methods
	bool CreateTopMenu();
	bool CreateSubMenu();
	bool MenuItemInitTop(UINT pos, UINT cmdId);
	bool MenuItemInitSeparator(HMENU menuHandle, UINT pos);
	bool MenuItemInitDelete(UINT pos, UINT cmdId);
	bool MenuItemInitVersions(UINT pos, UINT cmdId);
	bool MenuItemInitShare(UINT pos, UINT cmdId);

	// CommandInvocation handlers
	void Handle_CmdDelete();
	void Handle_CmdVersions();
	void Handle_CmdShare();
};