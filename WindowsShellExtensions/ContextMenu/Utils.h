#pragma once

#include<string>

#include <windows.h> /* required for registry */

#include <cpprest/http_client.h>

// error codes 
#define ERR_CODE			100000
#define ERR_HTTP_FAILED		ERR_CODE+1
#define ERR_EXCEPTION		ERR_CODE+2

class Utils {
public:
	Utils();

	/*
	* \brief		Root path as defined in the registry
	* \return		the root path
	*/
	static std::wstring GetRootPath();

	/*
	* \brief		Check whether a file is in the root path or not.
	* \return		returns true if the given object represented by the path is in the root path
	*/
	static bool IsInsideRootPath(std::wstring path);

	/*
	* \brief		Port where the server is listening as defined in the registry.
	* \return		api server port. 0 is returned if port cannot be read correctly.
	*/
	static int GetApiServerPort();

	/*
	* \brief		Creates an Uri for a context menu HTTP request.
	* \param		lastPathFragment	the last fragment of the URI, which represents the command (e.g. "share")
	* \return		uri pointing to the server
	*/
	static web::http::uri Utils::CreateUri(std::wstring lastPathFragment);


private:

	// cached server port during lifetime of extension. use GetApiServerPort
	static int m_cachedApiServerPort;

	// cached root path during lifetime of extension. use GetRootPath
	static std::wstring m_cachedRootPath;

	// checking string for prefix
	static bool IsPrefixOf(std::wstring prefix, std::wstring string);
	
	// read value in registry given key and name of the value
	static HRESULT GetHKCURegistryKeyAndValue(PCWSTR pszSubKey, PCWSTR pszValueName, PWSTR pszData, DWORD cbData);
};