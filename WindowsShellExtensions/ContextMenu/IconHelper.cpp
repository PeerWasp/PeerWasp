#include "IconHelper.h"

extern HINSTANCE g_hInst;
typedef DWORD ARGB;


// Based on: Visual Style Menus - http ://msdn.microsoft.com/en-us/library/bb757020.aspx
// The problem is that the "old" bitmaps do not support transparency, there is a hack that worked in the past: 
// the first pixel color was read and the color of it replaced with the background color of the menu in the whole bitmap.
// It seems that this does not work anymore with newer Windows versions (Vista+).
// The following helpers load a 32bit .ico icon file and convert it to a bitmap that can be used in the menus.


IconHelper::IconHelper(void) 
{
}

IconHelper::~IconHelper(void)
{
}

IWICImagingFactory *IconHelper::CreateWICFactory()
{
	IWICImagingFactory *WICFactory;
	CoCreateInstance(CLSID_WICImagingFactory, NULL, CLSCTX_INPROC_SERVER, IID_PPV_ARGS(&WICFactory));
	return WICFactory;
}

void IconHelper::InitBitmapInfo(__out_bcount(cbInfo) BITMAPINFO *pbmi, ULONG cbInfo, LONG cx, LONG cy, WORD bpp)
{
	ZeroMemory(pbmi, cbInfo);
	pbmi->bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
	pbmi->bmiHeader.biPlanes = 1;
	pbmi->bmiHeader.biCompression = BI_RGB;

	pbmi->bmiHeader.biWidth = cx;
	pbmi->bmiHeader.biHeight = cy;
	pbmi->bmiHeader.biBitCount = bpp;
}

HRESULT IconHelper::Create32BitHBITMAP(HDC hdc, const SIZE *psize, __deref_opt_out void **ppvBits, __out HBITMAP* phBmp)
{
	*phBmp = NULL;

	BITMAPINFO bmi;
	InitBitmapInfo(&bmi, sizeof(bmi), psize->cx, psize->cy, 32);

	HDC hdcUsed = hdc ? hdc : GetDC(NULL);
	if (hdcUsed)
	{
		*phBmp = CreateDIBSection(hdcUsed, &bmi, DIB_RGB_COLORS, ppvBits, NULL, 0);
		if (hdc != hdcUsed)
		{
			ReleaseDC(NULL, hdcUsed);
		}
	}
	return (NULL == *phBmp) ? E_OUTOFMEMORY : S_OK;
}


HRESULT IconHelper::LoadBitmapByIcon(UINT iconId, HBITMAP *bitmap)
{
	HRESULT hr = E_FAIL;

	*bitmap = NULL;
	IWICImagingFactory *WICFactory = NULL;
	IWICBitmap *pBitmap = NULL;
	HICON hicon = NULL;

	hr = LoadIconMetric(g_hInst, MAKEINTRESOURCE(iconId), LIM_SMALL, &hicon);

	if (SUCCEEDED(hr))
	{
		WICFactory = CreateWICFactory();
		hr = WICFactory->CreateBitmapFromHICON(hicon, &pBitmap);
		if (SUCCEEDED(hr))
		{
			IWICFormatConverter *pConverter;
			hr = WICFactory->CreateFormatConverter(&pConverter);
			if (SUCCEEDED(hr))
			{
				hr = pConverter->Initialize(pBitmap, GUID_WICPixelFormat32bppPBGRA, WICBitmapDitherTypeNone, NULL, 0.0f, WICBitmapPaletteTypeCustom);
				if (SUCCEEDED(hr))
				{
					UINT cx, cy;
					hr = pConverter->GetSize(&cx, &cy);
					if (SUCCEEDED(hr))
					{
						const SIZE sizIcon = { (int)cx, -(int)cy };
						BYTE *pbBuffer;
						hr = Create32BitHBITMAP(NULL, &sizIcon, reinterpret_cast<void **>(&pbBuffer), bitmap);
						if (SUCCEEDED(hr))
						{
							const UINT cbStride = cx * sizeof(ARGB);
							const UINT cbBuffer = cy * cbStride;
							hr = pConverter->CopyPixels(NULL, cbStride, cbBuffer, pbBuffer);
						}
					}
				}

				pConverter->Release();
			}

			pBitmap->Release();
		}


		// release wic factory
		if (WICFactory != NULL)
		{
			WICFactory->Release();
			WICFactory = NULL;
		}
	}

	if (SUCCEEDED(hr) && (*bitmap != NULL)) {
		hr = S_OK;
	}
	else {
		hr = E_FAIL;
	}

	return hr;
}