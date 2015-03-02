#pragma once

#include <map>
#include <string>

// resource id representing no icon, i.e. command without icon
#define NO_ICON 0

// unique id for each command
enum CommandId {
	CMD_TOP,
	CMD_DELETE,
	CMD_VERSIONS,
	CMD_SHARE,
};

struct CommandInfo {
	// a unique id in the CommandId enum
	CommandId id;
	// text displayed in context menu
	std::wstring menuText;
	// help text displayed in bottom status bar
	std::wstring helpText;
	// id to a icon resource (in resource.h)
	int bitmapResourceId;

};

class Command
{
public:
	/* 
	* \brief		Creates a map with all available commands and their metadata such as text to display, icon, etc.
	* \return		mapping from command ID to Command
	*/
	static std::map<CommandId, CommandInfo> CreateCommandMap();

	/*
	* \brief		Determines whether a command has an associated icon.
	* \param[in]	Command
	* \return		true if command has an icon. Otherwise false.
	*/ 
	static bool hasIcon(struct CommandInfo *cmd);
};
