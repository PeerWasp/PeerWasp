#include "Command.h"

#include "resource.h"

#pragma region commandmap

std::map<CommandId, CommandInfo> Command::CreateCommandMap()
{
	std::map<CommandId, CommandInfo> m;

	struct CommandInfo cmdTop;
	cmdTop.id = CMD_TOP;
	cmdTop.menuText = L"PeerBox";
	cmdTop.helpText = L"PeerBox";
	cmdTop.bitmapResourceId = IDB_PEERBOX;
	m[CMD_TOP] = cmdTop;

	struct CommandInfo cmdDelete;
	cmdDelete.id = CMD_DELETE;
	cmdDelete.menuText = L"&Delete";
	cmdDelete.helpText = L"Delete the selected files and folders.";
	cmdDelete.bitmapResourceId = IDB_DELETE;
	m[CMD_DELETE] = cmdDelete;

	struct CommandInfo cmdVersions;
	cmdVersions.id = CMD_VERSIONS;
	cmdVersions.menuText = L"&Versions";
	cmdVersions.helpText = L"Show and restore previous versions of this file.";
	cmdVersions.bitmapResourceId = IDB_VERSIONS;
	m[CMD_VERSIONS] = cmdVersions;

	struct CommandInfo cmdShare;
	cmdShare.id = CMD_SHARE;
	cmdShare.menuText = L"&Share";
	cmdShare.helpText = L"Share the selected folder with other users.";
	cmdShare.bitmapResourceId = IDB_SHARE;
	m[CMD_SHARE] = cmdShare;

	return m;
}

#pragma endregion