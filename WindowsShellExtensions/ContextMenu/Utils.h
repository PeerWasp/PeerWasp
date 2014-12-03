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

	// returns the root path
	static std::wstring GetRootPath();

	// returns true if the given object represented by the path is in the root path
	static bool IsInsideRootPath(std::wstring path);

	static int GetApiServerPort();

	static web::http::uri Utils::CreateUri(std::wstring lastPathFragment);


private:

	static int m_cachedApiServerPort;

	// cached root path during lifetime of extension. use GetRootPath!
	static std::wstring m_cachedRootPath;

	static bool IsPrefixOf(std::wstring prefix, std::wstring string);
	
	static HRESULT GetHKCURegistryKeyAndValue(PCWSTR pszSubKey, PCWSTR pszValueName, PWSTR pszData, DWORD cbData);
};