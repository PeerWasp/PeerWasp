
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

#pragma comment(lib, "shlwapi") // Default link libs do not include this.
#pragma comment(lib, "comctl32")
#pragma comment(lib, "WindowsCodecs")    // WIC

class IconHelper {
public:
	static HRESULT LoadBitmapByIcon(UINT iconId, HBITMAP *bitmap);

private:
	IconHelper(void);
	~IconHelper(void);

	static HRESULT Create32BitHBITMAP(HDC hdc, const SIZE *psize, __deref_opt_out void **ppvBits, __out HBITMAP* phBmp);
	static void InitBitmapInfo(__out_bcount(cbInfo) BITMAPINFO *pbmi, ULONG cbInfo, LONG cx, LONG cy, WORD bpp);
	
	static HRESULT CreateWICFactory(IWICImagingFactory **WICFactory);

};