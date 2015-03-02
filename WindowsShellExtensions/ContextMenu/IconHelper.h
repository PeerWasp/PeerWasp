
#define _WIN7_PLATFORM_UPDATE

#include <wincodec.h>           // WIC
#include <windows.h>


#include <windows.h>
#include <windowsx.h>           // For WM_COMMAND handling macros
#include <shlobj.h>             // For shell
#include <shlwapi.h>            // QISearch, easy way to implement QI
#include <commctrl.h>
#include <wincodec.h>           // WIC
#include "resource.h"

#pragma comment(lib, "shlwapi")			// Default link libs do not include this.
#pragma comment(lib, "comctl32")
#pragma comment(lib, "WindowsCodecs")	// WIC

class IconHelper {
public:
	/*
	* \brief		Loads bitmap given resource id pointing to an icon (.ico).
	* \param[in]	iconId	resource id
	* \param[out]	bitmap	the bitmap, may be null if loading fails
	* \return		status of operation
	*/
	static HRESULT LoadBitmapByIcon(UINT iconId, HBITMAP *bitmap);

private:
	IconHelper(void);
	~IconHelper(void);

	// creates a bitmap where icon can be copied to
	static HRESULT Create32BitHBITMAP(HDC hdc, const SIZE *psize, __deref_opt_out void **ppvBits, __out HBITMAP* phBmp);
	// initializes bitmap info structure
	static void InitBitmapInfo(__out_bcount(cbInfo) BITMAPINFO *pbmi, ULONG cbInfo, LONG cx, LONG cy, WORD bpp);
	// creates a Windows Imaging Component factory, used to create and convert icons 
	static HRESULT CreateWICFactory(IWICImagingFactory **WICFactory);

};