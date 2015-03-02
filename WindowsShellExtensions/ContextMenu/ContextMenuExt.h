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
	// Initialize the context menu handler. Called when user opens context menu and allows
	// accessing currently marked files and folders.
    IFACEMETHODIMP Initialize(LPCITEMIDLIST pidlFolder, LPDATAOBJECT pDataObj, HKEY hKeyProgID);

    // IContextMenu
	// Allows adding custom menu to the shell context menu. Called after initialization.
    IFACEMETHODIMP QueryContextMenu(HMENU hMenu, UINT indexMenu, UINT idCmdFirst, UINT idCmdLast, UINT uFlags);
	// Executing a command. Called when user clicks on menu item.
    IFACEMETHODIMP InvokeCommand(LPCMINVOKECOMMANDINFO pici);
	// Allows displaying help text. Called when user highlights a menu element (not clicking).
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





	/***************
	** Building menu: called when user opens context menu (e.g. right click).
	****************/
	/*
	* \brief		Creates the top level menu in the context menu
	*/
	bool CreateTopMenu();

	/*
	* \brief		Creates the actual menu with application specific entries (commands).
	*/
	bool CreateSubMenu();
	// create menu entries 
	bool MenuItemInitTop(UINT pos, UINT cmdId);
	bool MenuItemInitSeparator(HMENU menuHandle, UINT pos);
	bool MenuItemInitDelete(UINT pos, UINT cmdId);
	bool MenuItemInitVersions(UINT pos, UINT cmdId);
	bool MenuItemInitShare(UINT pos, UINT cmdId);

	/*
	* \brief		Loads an icon (*.ico) file and converts it to a bitmap that
	*				is displayed in the context menu.
	* \param[in]	mii		menu item info as given by the API
	* \param[in]	cmd		the command
	*/
	HRESULT LoadAndSetBitmapByIcon(MENUITEMINFO *mii, CommandId cmd);

	/*
	* \brief		updates the selection flags depending on the current selection of files
	* \param		currentPath	a path to a file or folder
	*/
	void UpdateSelectionFlags(std::wstring currentPath);

	/*
	* \brief		Get help text for a command item.
	* \param		commandId	id is the key in the m_cmdIdToCommand map
	* \return		help text
	*/
	std::wstring ContextMenuExt::GetHelpText(UINT commandId);



	/***************
	** CommandInvocation handlers: called when user clicks on menu item.
	****************/
	/*
	* \brief		Handling of Delete command.
	* \return		error code
	*/
	int Handle_CmdDelete();

	/*
	* \brief		Handling of Versions command
	* \return		error code
	*/
	int Handle_CmdVersions();

	/*
	* \brief		Handling of Share command
	* \return		error code
	*/
	int Handle_CmdShare();


	/***************
	** User feedback: message boxes
	****************/
	/*
	* \brief		Shows a message box indicating that the communication between 
	*				context menu and the server is not possible. Server probably offline or wrong port.
	* \param		hwnd owner window handle
	*/
	void ShowServerNotRunningMessage(HWND hwnd);

	/*
	* \brief		Shows a message box indicating that an unexpected error occurred. This is usually the case
	*				if an exception is thrown somewhere.
	* \param		hwnd owner window handle
	*/
	void ShowUnexpectedErrorMessage(HWND hwnd);
};