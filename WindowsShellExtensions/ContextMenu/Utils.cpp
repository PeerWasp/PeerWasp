#include <algorithm>
#include "Utils.h"


// static initialization -- empty string means not path yet
std::wstring Utils::m_cachedRootPath = L"";

// port to use for api calls
int Utils::m_cachedApiServerPort = 0;

Utils::Utils()
{

}

// checks whether path points to object inside root path
bool Utils::IsInsideRootPath(std::wstring path)
{
	return 
		/* path is longer than the rootpath itself, i.e. 'inside' */
		(GetRootPath().size() < path.size())
		/*root path is a prefix of path */
		&& IsPrefixOf(GetRootPath(), path);
}

// returns true if parameter prefix is a prefix of the parameter string
bool Utils::IsPrefixOf(std::wstring prefix, std::wstring string)
{
	// first mismatch needs to be at the end of prefix.
	return std::mismatch(prefix.begin(), prefix.end(), string.begin()).first == prefix.end();
}

std::wstring Utils::GetRootPath()
{
	if (m_cachedRootPath.compare(L"") == 0) {
		// registry lookup
		HRESULT hr;
		wchar_t value[260] = { '\0' };
		hr = GetHKCURegistryKeyAndValue(L"Software\\Peerbox", L"rootpath", value, sizeof(value));
		if (SUCCEEDED(hr) && value[0] != L'\0')
		{
			m_cachedRootPath = std::wstring(value);
		}
	}

	return m_cachedRootPath;
}

web::http::uri Utils::CreateUri(std::wstring lastPathFragment)
{
	web::http::uri_builder builder;
	builder.set_scheme(L"http");
	builder.set_host(L"localhost");
	builder.set_port(GetApiServerPort());
	builder.append_path(L"contextmenu");
	builder.append_path(lastPathFragment);
	return builder.to_uri();
		
}

int Utils::GetApiServerPort()
{
	if (m_cachedApiServerPort <= 0 || m_cachedApiServerPort > 65535)
	{
		// registry lookup
		HRESULT hr;
		int value = 0;
		hr = GetHKCURegistryKeyAndValue(L"Software\\Peerbox", L"api_server_port", (PWSTR)&value, sizeof(value));
		if (SUCCEEDED(hr) && value != 0)
		{
			m_cachedApiServerPort = value;
		}
	}
	return m_cachedApiServerPort;
}

HRESULT Utils::GetHKCURegistryKeyAndValue(PCWSTR pszSubKey, PCWSTR pszValueName, PWSTR pszData, DWORD cbData)
{
	HRESULT hr;
	HKEY hKey = NULL;

	// Try to open the specified registry key. 
	hr = HRESULT_FROM_WIN32(RegOpenKeyEx(HKEY_CURRENT_USER, pszSubKey, 0,
		KEY_READ, &hKey));

	if (SUCCEEDED(hr))
	{
		// Get the data for the specified value name.
		hr = HRESULT_FROM_WIN32(RegQueryValueEx(hKey, pszValueName, NULL,
			NULL, reinterpret_cast<LPBYTE>(pszData), &cbData));
	}

	if (hKey != NULL) {
		RegCloseKey(hKey);
	}

	return hr;
}

