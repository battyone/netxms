/*
 ** NetXMS - Network Management System
 ** Copyright (C) 2003-2020 Raden Solutions
 **
 ** This program is free software; you can redistribute it and/or modify
 ** it under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 3 of the License, or
 ** (at your option) any later version.
 **
 ** This program is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ** GNU General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this program; if not, write to the Free Software
 ** Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 **
 ** File: unicode.cpp
 **
 **/

#include "libnetxms.h"
#include "unicode_cc.h"
#include <nxcrypto.h>

/**
 * Default codepage
 */
char g_cpDefault[MAX_CODEPAGE_LEN] = ICONV_DEFAULT_CODEPAGE;
CodePageType g_defaultCodePageType = CodePageType::ISO8859_1;

/**
 * Set application's default codepage
 */
bool LIBNETXMS_EXPORTABLE SetDefaultCodepage(const char *cp)
{
   bool rc;
#if HAVE_ICONV && !defined(__DISABLE_ICONV)
   iconv_t cd;

   cd = iconv_open(cp, "UTF-8");
   if (cd != (iconv_t)(-1))
   {
      iconv_close(cd);
#endif
      strlcpy(g_cpDefault, cp, MAX_CODEPAGE_LEN);
      rc = true;

      if (!stricmp(cp, "ASCII"))
         g_defaultCodePageType = CodePageType::ASCII;
      else if (!stricmp(cp, "ISO-8859-1") || !stricmp(cp, "ISO_8859_1") ||
               !stricmp(cp, "ISO8859-1") || !stricmp(cp, "ISO8859_1") ||
               !stricmp(cp, "LATIN-1") || !stricmp(cp, "LATIN1"))
         g_defaultCodePageType = CodePageType::ISO8859_1;
      else
         g_defaultCodePageType = CodePageType::OTHER;

#if HAVE_ICONV && !defined(__DISABLE_ICONV)
   }
   else
   {
      rc = false;
   }
#endif
   return rc;
}

#ifndef UNICODE_UCS2

/**
 * Calculate length of UCS-2 character string
 */
size_t LIBNETXMS_EXPORTABLE ucs2_strlen(const UCS2CHAR *s)
{
   size_t len = 0;
   const UCS2CHAR *curr = s;
   while(*curr++)
      len++;
   return len;
}

#endif

#ifndef UNICODE_UCS4

/**
 * Calculate length of UCS-4 character string
 */
size_t LIBNETXMS_EXPORTABLE ucs4_strlen(const UCS4CHAR *s)
{
   size_t len = 0;
   const UCS4CHAR *curr = s;
   while(*curr++)
      len++;
   return len;
}

#endif

#if !HAVE_WCSLEN

/**
 * Calculate length of wide character string
 */
size_t LIBNETXMS_EXPORTABLE wcslen(const WCHAR *s)
{
   size_t len = 0;
   const WCHAR *curr = s;
   while(*curr++)
      len++;
   return len;
}

#endif

#ifndef UNICODE_UCS2

/**
 * Duplicate UCS-2 character string
 */
UCS2CHAR LIBNETXMS_EXPORTABLE *ucs2_strdup(const UCS2CHAR *src)
{
   return (UCS2CHAR *)MemCopyBlock(src, (ucs2_strlen(src) + 1) * sizeof(UCS2CHAR));
}

#endif

#ifndef UNICODE_UCS4

/**
 * Duplicate UCS-4 character string
 */
UCS4CHAR LIBNETXMS_EXPORTABLE *ucs4_strdup(const UCS4CHAR *src)
{
   return (UCS4CHAR *)MemCopyBlock(src, (ucs4_strlen(src) + 1) * sizeof(UCS4CHAR));
}

#endif

#if !UNICODE_UCS2

/**
 * Copy UCS-2 character string with length limitation
 */
UCS2CHAR LIBNETXMS_EXPORTABLE *ucs2_strncpy(UCS2CHAR *dest, const UCS2CHAR *src, size_t n)
{
   size_t len = ucs2_strlen(src) + 1;
   if (len > n)
      len = n;
   memcpy(dest, src, len * sizeof(UCS2CHAR));
   return dest;
}

#endif

#if !UNICODE_UCS4

/**
 * Copy UCS-2 character string with length limitation
 */
UCS4CHAR LIBNETXMS_EXPORTABLE *ucs4_strncpy(UCS4CHAR *dest, const UCS4CHAR *src, size_t n)
{
   size_t len = ucs4_strlen(src) + 1;
   if (len > n)
      len = n;
   memcpy(dest, src, len * sizeof(UCS4CHAR));
   return dest;
}

#endif

#if !HAVE_WCSNCPY

/**
 * Copy UCS-2 character string with length limitation
 */
WCHAR LIBNETXMS_EXPORTABLE *wcsncpy(WCHAR *dest, const WCHAR *src, size_t n)
{
   size_t len = wcslen(src) + 1;
   if (len > n)
      len = n;
   memcpy(dest, src, len * sizeof(WCHAR));
   return dest;
}

#endif

#ifndef _WIN32

#ifndef __DISABLE_ICONV

/**
 * Convert UNICODE string to single-byte string using iconv
 */
static int WideCharToMultiByteIconv(int iCodePage, DWORD dwFlags, const WCHAR *pWideCharStr,
         int cchWideChar, char *pByteStr, int cchByteChar, char *pDefaultChar, BOOL *pbUsedDefChar)
{
   iconv_t cd;
   int nRet;
   const char *inbuf;
   char *outbuf;
   size_t inbytes, outbytes;
   char cp[MAX_CODEPAGE_LEN + 16];

   strcpy(cp, g_cpDefault);
#if HAVE_ICONV_IGNORE
   strcat(cp, "//IGNORE");
#endif /* HAVE_ICONV_IGNORE */

   cd = IconvOpen(iCodePage == CP_UTF8 ? "UTF-8" : cp, UNICODE_CODEPAGE_NAME);
   if (cd == (iconv_t)(-1))
   {
#if UNICODE_UCS4
      return ucs4_to_ASCII(pWideCharStr, cchWideChar, pByteStr, cchByteChar);
#else
      return ucs2_to_ASCII(pWideCharStr, cchWideChar, pByteStr, cchByteChar);
#endif
   }

   inbuf = (const char *)pWideCharStr;
   inbytes = ((cchWideChar == -1) ? wcslen(pWideCharStr) + 1 : cchWideChar) * sizeof(WCHAR);
   outbuf = pByteStr;
   outbytes = cchByteChar;
   nRet = iconv(cd, (ICONV_CONST char **)&inbuf, &inbytes, &outbuf, &outbytes);
   IconvClose(cd);
   if (nRet == -1)
   {
      if (errno == EILSEQ)
      {
         nRet = cchByteChar - outbytes;
      }
      else
      {
         nRet = 0;
      }
   }
   else
   {
      nRet = cchByteChar - outbytes;
   }
   if (outbytes > 0)
   {
      *outbuf = 0;
   }

   return nRet;
}

#endif

/**
 * Convert UNICODE string to single-byte string (Windows compatibility layer)
 */
int LIBNETXMS_EXPORTABLE WideCharToMultiByte(int iCodePage, DWORD dwFlags, const WCHAR *pWideCharStr,
         int cchWideChar, char *pByteStr, int cchByteChar, char *pDefaultChar, BOOL *pbUsedDefChar)
{
   if (iCodePage == CP_UTF8)
   {
#ifdef UNICODE_UCS4
      if (cchByteChar == 0)
         return ucs4_utf8len(pWideCharStr, cchWideChar);
      return ucs4_to_utf8(pWideCharStr, cchWideChar, pByteStr, cchByteChar);
#else
      if (cchByteChar == 0)
         return ucs2_utf8len(pWideCharStr, cchWideChar);
      return ucs2_to_utf8(pWideCharStr, cchWideChar, pByteStr, cchByteChar);
#endif
   }

#if HAVE_ICONV && !defined(__DISABLE_ICONV)
   if (cchByteChar == 0)
   {
      // Calculate required length. Because iconv cannot calculate
      // resulting multibyte string length, assume the worst case - 2 bytes per character
      return ((cchWideChar == -1) ? wcslen(pWideCharStr) : cchWideChar) * 2 + 1;
   }

#if UNICODE_UCS4
   if (g_defaultCodePageType == CodePageType::ISO8859_1)
      return ucs4_to_ISO8859_1(pWideCharStr, cchWideChar, pByteStr, cchByteChar);
   if (g_defaultCodePageType == CodePageType::ASCII)
      return ucs4_to_ASCII(pWideCharStr, cchWideChar, pByteStr, cchByteChar);
#else
   if (g_defaultCodePageType == CodePageType::ISO8859_1)
      return ucs2_to_ISO8859_1(pWideCharStr, cchWideChar, pByteStr, cchByteChar);
   if (g_defaultCodePageType == CodePageType::ASCII)
      return ucs2_to_ASCII(pWideCharStr, cchWideChar, pByteStr, cchByteChar);
#endif

   return WideCharToMultiByteIconv(iCodePage, dwFlags, pWideCharStr, cchWideChar, pByteStr, cchByteChar, pDefaultChar, pbUsedDefChar);
#else
   if (cchByteChar == 0)
   {
      return wcslen(pWideCharStr) + 1;
   }

#if UNICODE_UCS4
   if (g_defaultCodePageType == CodePageType::ISO8859_1)
      return ucs4_to_ISO8859_1(pWideCharStr, cchWideChar, pByteStr, cchByteChar);
   return ucs4_to_ASCII(pWideCharStr, cchWideChar, pByteStr, cchByteChar);
#else
   if (g_defaultCodePageType == CodePageType::ISO8859_1)
      return ucs2_to_ISO8859_1(pWideCharStr, cchWideChar, pByteStr, cchByteChar);
   return ucs2_to_ASCII(pWideCharStr, cchWideChar, pByteStr, cchByteChar);
#endif
#endif
}

#ifndef __DISABLE_ICONV

/**
 * Convert single-byte to UNICODE string using iconv
 */
static int MultiByteToWideCharIconv(int iCodePage, DWORD dwFlags, const char *pByteStr, int cchByteChar, WCHAR *pWideCharStr, int cchWideChar)
{
   iconv_t cd;
   int nRet;
   const char *inbuf;
   char *outbuf;
   size_t inbytes, outbytes;

   cd = IconvOpen(UNICODE_CODEPAGE_NAME, iCodePage == CP_UTF8 ? "UTF-8" : g_cpDefault);
   if (cd == (iconv_t)(-1))
   {
#if UNICODE_UCS4
      return ASCII_to_ucs4(pByteStr, cchByteChar, pWideCharStr, cchWideChar);
#else
      return ASCII_to_ucs2(pByteStr, cchByteChar, pWideCharStr, cchWideChar);
#endif
   }

   inbuf = pByteStr;
   inbytes = (cchByteChar == -1) ? strlen(pByteStr) + 1 : cchByteChar;
   outbuf = (char *)pWideCharStr;
   outbytes = cchWideChar * sizeof(WCHAR);
   nRet = iconv(cd, (ICONV_CONST char **)&inbuf, &inbytes, &outbuf, &outbytes);
   IconvClose(cd);

   if (nRet == -1)
   {
      if (errno == EILSEQ)
      {
         nRet = (cchWideChar * sizeof(WCHAR) - outbytes) / sizeof(WCHAR);
      }
      else
      {
         nRet = 0;
      }
   }
   else
   {
      nRet = (cchWideChar * sizeof(WCHAR) - outbytes) / sizeof(WCHAR);
   }
   if (((char *) outbuf - (char *) pWideCharStr > sizeof(WCHAR)) && (*pWideCharStr == 0xFEFF))
   {
      // Remove UNICODE byte order indicator if presented
      memmove(pWideCharStr, &pWideCharStr[1], (char *) outbuf - (char *) pWideCharStr - sizeof(WCHAR));
      outbuf -= sizeof(WCHAR);
      nRet--;
   }
   if (outbytes >= sizeof(WCHAR))
   {
      *((WCHAR *)outbuf) = 0;
   }

   return nRet;
}

#endif   /* __DISABLE_ICONV */

/**
 * Convert single-byte to UNICODE string (Windows compatibility layer)
 */
int LIBNETXMS_EXPORTABLE MultiByteToWideChar(int iCodePage, DWORD dwFlags, const char *pByteStr, int cchByteChar, WCHAR *pWideCharStr, int cchWideChar)
{
   if (iCodePage == CP_UTF8)
   {
#if UNICODE_UCS4
      if (cchWideChar == 0)
         return utf8_ucs4len(pByteStr, cchByteChar);
      return utf8_to_ucs4(pByteStr, cchByteChar, pWideCharStr, cchWideChar);
#else
      if (cchWideChar == 0)
         return utf8_ucs2len(pByteStr, cchByteChar);
      return utf8_to_ucs2(pByteStr, cchByteChar, pWideCharStr, cchWideChar);
#endif
   }

   if (cchWideChar == 0)
      return strlen(pByteStr) + 1;

#if UNICODE_UCS4
   if (g_defaultCodePageType == CodePageType::ISO8859_1)
      return ISO8859_1_to_ucs4(pByteStr, cchByteChar, pWideCharStr, cchWideChar);
   if (g_defaultCodePageType == CodePageType::ASCII)
      return ASCII_to_ucs4(pByteStr, cchByteChar, pWideCharStr, cchWideChar);
#else
   if (g_defaultCodePageType == CodePageType::ISO8859_1)
      return ISO8859_1_to_ucs2(pByteStr, cchByteChar, pWideCharStr, cchWideChar);
   if (g_defaultCodePageType == CodePageType::ASCII)
      return ASCII_to_ucs2(pByteStr, cchByteChar, pWideCharStr, cchWideChar);
#endif

#if HAVE_ICONV && !defined(__DISABLE_ICONV)
   return MultiByteToWideCharIconv(iCodePage, dwFlags, pByteStr, cchByteChar, pWideCharStr, cchWideChar);
#else
#if UNICODE_UCS4
   return ASCII_to_ucs4(pByteStr, cchByteChar, pWideCharStr, cchWideChar);
#else
   return ASCII_to_ucs2(pByteStr, cchByteChar, pWideCharStr, cchWideChar);
#endif
#endif
}

#endif   /* _WIN32 */

/**
 * Convert multibyte string to wide string using current LC_CTYPE setting and
 * allocating wide string dynamically
 */
WCHAR LIBNETXMS_EXPORTABLE *WideStringFromMBStringSysLocale(const char *src)
{
#ifdef _WIN32
   return WideStringFromMBString(src);
#else
   if (src == nullptr)
      return nullptr;
   size_t len = strlen(src) + 1;
   WCHAR *out = MemAllocStringW(len);
#if HAVE_MBSTOWCS
   mbstowcs(out, src, len);
#else
   mb_to_wchar(src, -1, out, len);
#endif
   return out;
#endif
}

/**
 * Convert multibyte string to wide string using current codepage and
 * allocating wide string dynamically
 */
WCHAR LIBNETXMS_EXPORTABLE *WideStringFromMBString(const char *src)
{
   if (src == nullptr)
      return nullptr;
   size_t len = strlen(src) + 1;
   WCHAR *out = MemAllocStringW(len);
   mb_to_wchar(src, -1, out, len);
   return out;
}

/**
 * Convert wide string to UTF8 string allocating wide string dynamically
 */
WCHAR LIBNETXMS_EXPORTABLE *WideStringFromUTF8String(const char *src)
{
   if (src == nullptr)
      return nullptr;
   size_t len = strlen(src) + 1;
   WCHAR *out = MemAllocStringW(len);
   utf8_to_wchar(src, -1, out, len);
   return out;
}

/**
 * Convert wide string to multibyte string using current codepage and
 * allocating multibyte string dynamically
 */
char LIBNETXMS_EXPORTABLE *MBStringFromWideString(const WCHAR *src)
{
   if (src == nullptr)
      return nullptr;
   size_t len = wcslen(src) + 1;
   char *out = MemAllocStringA(len);
   wchar_to_mb(src, -1, out, len);
   return out;
}

/**
 * Convert wide string to multibyte string using current LC_CTYPE setting and
 * allocating multibyte string dynamically
 */
char LIBNETXMS_EXPORTABLE *MBStringFromWideStringSysLocale(const WCHAR *src)
{
#ifdef _WIN32
   return MBStringFromWideString(src);
#else
   if (src == nullptr)
      return nullptr;
   size_t len = wcslen(src) * 3 + 1;  // add extra bytes in case of UTF-8 as target encoding
   char *out = MemAllocStringA(len);
#if HAVE_WCSTOMBS
   wcstombs(out, src, len);
#else
   wchar_to_mb(src, -1, out, len);
#endif
   return out;
#endif
}

/**
 * Convert wide string to UTF8 string allocating UTF8 string dynamically
 */
char LIBNETXMS_EXPORTABLE *UTF8StringFromWideString(const WCHAR *src)
{
   int len = WideCharToMultiByte(CP_UTF8, 0, src, -1, nullptr, 0, nullptr, nullptr);
   char *out = (char *)MemAlloc(len);
   WideCharToMultiByte(CP_UTF8, 0, src, -1, out, len, nullptr, nullptr);
   return out;
}

/**
 * Convert UTF8 string to multibyte string using current codepage and
 * allocating multibyte string dynamically
 */
char LIBNETXMS_EXPORTABLE *MBStringFromUTF8String(const char *s)
{
   size_t len = strlen(s) + 1;
   char *out = MemAllocStringA(len);
   utf8_to_mb(s, -1, out, len);
   return out;
}

/**
 * Convert multibyte string to UTF8 string allocating UTF8 string dynamically
 */
char LIBNETXMS_EXPORTABLE *UTF8StringFromMBString(const char *s)
{
   size_t len = strlen(s) * 3 + 1;   // assume worst case - 3 bytes per character
   char *out = MemAllocStringA(len);
   mb_to_utf8(s, -1, out, len);
   return out;
}

/**
 * Convert UCS-4 string to UCS-2 string allocating UCS-2 string dynamically
 */
UCS2CHAR LIBNETXMS_EXPORTABLE *UCS2StringFromUCS4String(const UCS4CHAR *src)
{
   size_t len = ucs4_ucs2len(src, -1);
   UCS2CHAR *out = MemAllocArrayNoInit<UCS2CHAR>(len);
   ucs4_to_ucs2(src, -1, out, len);
   return out;
}

/**
 * Convert UCS-2 string to UCS-4 string allocating UCS-4 string dynamically
 */
UCS4CHAR LIBNETXMS_EXPORTABLE *UCS4StringFromUCS2String(const UCS2CHAR *src)
{
   size_t len = ucs2_strlen(src) + 1;
   UCS4CHAR *out = MemAllocArrayNoInit<UCS4CHAR>(len);
   ucs2_to_ucs4(src, -1, out, static_cast<int>(len));
   return out;
}

#ifndef UNICODE_UCS2

/**
 * Convert UTF-8 to UCS-2 (dynamically allocated output string)
 */
UCS2CHAR LIBNETXMS_EXPORTABLE *UCS2StringFromUTF8String(const char *utf8String)
{
   if (utf8String == nullptr)
      return nullptr;
   size_t len = strlen(utf8String) + 1;
   UCS2CHAR *out = MemAllocArrayNoInit<UCS2CHAR>(len);
   utf8_to_ucs2(utf8String, -1, out, len);
   return out;
}

/**
 * Convert UCS-2 string to UTF-8 string allocating UTF-8 string dynamically
 */
char LIBNETXMS_EXPORTABLE *UTF8StringFromUCS2String(const UCS2CHAR *src)
{
   size_t len = ucs2_utf8len(src, -1);
   char *out = MemAllocStringA(len);
   ucs2_to_utf8(src, -1, out, len);
   return out;
}

/**
 * Convert multibyte string to UCS-2 string allocating UCS-2 string dynamically
 */
UCS2CHAR LIBNETXMS_EXPORTABLE *UCS2StringFromMBString(const char *src)
{
   size_t len = strlen(src) + 1;
   UCS2CHAR *out = MemAllocArrayNoInit<UCS2CHAR>(len);
   mb_to_ucs2(src, -1, out, len);
   return out;
}

/**
 * Convert UCS-2 string to multibyte string allocating multibyte string dynamically
 */
char LIBNETXMS_EXPORTABLE *MBStringFromUCS2String(const UCS2CHAR *src)
{
   size_t len = ucs2_strlen(src) + 1;
   char *out = MemAllocStringA(len);
   ucs2_to_mb(src, -1, out, len);
   return out;
}

#endif /* UNICODE_UCS2 */

#ifndef UNICODE_UCS4

/**
 * Convert multibyte string to UCS-4 string allocating UCS-4 string dynamically
 */
UCS4CHAR LIBNETXMS_EXPORTABLE *UCS4StringFromMBString(const char *src)
{
   size_t len = strlen(src) + 1;
   UCS4CHAR *out = MemAllocArrayNoInit<UCS4CHAR>(len);
   mb_to_ucs4(src, -1, out, len);
   return out;
}

/**
 * Convert UCS-4 string to multibyte string allocating multibyte string dynamically
 */
char LIBNETXMS_EXPORTABLE *MBStringFromUCS4String(const UCS4CHAR *src)
{
   size_t len = ucs4_strlen(src) + 1;
   char *out = MemAllocStringA(len);
   ucs4_to_mb(src, -1, out, len);
   return out;
}

#endif /* UNICODE_UCS4 */

/**
 * UNIX UNICODE specific wrapper functions
 */
#if !defined(_WIN32)

#if !HAVE_WSTAT

int LIBNETXMS_EXPORTABLE wstat(const WCHAR *_path, struct stat *_sbuf)
{
   char path[MAX_PATH];

   WideCharToMultiByte(CP_ACP, WC_COMPOSITECHECK | WC_DEFAULTCHAR,
      _path, -1, path, MAX_PATH, NULL, NULL);
   return stat(path, _sbuf);
}

#endif /* !HAVE_WSTAT */

#if defined(UNICODE)

/**
 * Wide character version of some libc functions
 */

#define DEFINE_PATH_FUNC(func) \
int LIBNETXMS_EXPORTABLE w##func(const WCHAR *_path) \
{ \
	char path[MAX_PATH]; \
	WideCharToMultiByteSysLocale(_path, path, sizeof(path)); \
	return func(path); \
}

#if !HAVE_WCHDIR
DEFINE_PATH_FUNC(chdir)
#endif

#if !HAVE_WRMDIR
DEFINE_PATH_FUNC(rmdir)
#endif

#if !HAVE_WUNLINK
DEFINE_PATH_FUNC(unlink)
#endif

#if !HAVE_WREMOVE
DEFINE_PATH_FUNC(remove)
#endif

#if !HAVE_WMKSTEMP

int LIBNETXMS_EXPORTABLE wmkstemp(WCHAR *_path)
{
   char path[MAX_PATH];
   WideCharToMultiByteSysLocale(_path, path, sizeof(path));
   int rc = mkstemp(path);
   if (rc != -1)
   {
      MultiByteToWideCharSysLocale(path, _path, wcslen(_path) + 1);
   }
   return rc;
}

#endif

#if !HAVE_WPOPEN

FILE LIBNETXMS_EXPORTABLE *wpopen(const WCHAR *_command, const WCHAR *_type)
{
   char *command = MBStringFromWideStringSysLocale(_command);
   char type[64];
   wchar_to_mb(_type, -1, type, 64);
   FILE *f = popen(command, type);
   MemFree(command);
   return f;
}

#endif

#if !HAVE_WFOPEN

FILE LIBNETXMS_EXPORTABLE *wfopen(const WCHAR *_name, const WCHAR *_type)
{
   char name[MAX_PATH], type[128];
   WideCharToMultiByteSysLocale(_name, name, sizeof(name));
   WideCharToMultiByteSysLocale(_type, type, sizeof(type));
   return fopen(name, type);
}

#endif

#if HAVE_FOPEN64 && !HAVE_WFOPEN64

FILE LIBNETXMS_EXPORTABLE *wfopen64(const WCHAR *_name, const WCHAR *_type)
{
   char name[MAX_PATH], type[128];
   WideCharToMultiByteSysLocale(_name, name, sizeof(name));
   WideCharToMultiByteSysLocale(_type, type, sizeof(type));
   return fopen64(name, type);
}

#endif

#if !HAVE_WOPEN

int LIBNETXMS_EXPORTABLE wopen(const WCHAR *_name, int flags, ...)
{
   char name[MAX_PATH];
   WideCharToMultiByteSysLocale(_name, name, sizeof(name));
   int rc;
   if (flags & O_CREAT)
   {
      va_list args;
      va_start(args, flags);
      rc = open(name, flags, (mode_t)va_arg(args, int));
      va_end(args);
   }
   else
   {
      rc = open(name, flags);
   }
   return rc;
}

#endif

#if !HAVE_WCHMOD

int LIBNETXMS_EXPORTABLE wchmod(const WCHAR *_path, int mode)
{
   char path[MAX_PATH];
   WideCharToMultiByteSysLocale(_path, path, sizeof(path));
   return chmod(path, mode);
}

#endif

#if !HAVE_WRENAME

int LIBNETXMS_EXPORTABLE wrename(const WCHAR *_oldpath, const WCHAR *_newpath)
{
   char oldpath[MAX_PATH], newpath[MAX_PATH];
   WideCharToMultiByteSysLocale(_oldpath, oldpath, sizeof(oldpath));
   WideCharToMultiByteSysLocale(_newpath, newpath, sizeof(newpath));
   return rename(oldpath, newpath);
}

#endif

#if !HAVE_WSYSTEM

int LIBNETXMS_EXPORTABLE wsystem(const WCHAR *_cmd)
{
   char *cmd = MBStringFromWideStringSysLocale(_cmd);
   int rc = system(cmd);
   MemFree(cmd);
   return rc;
}

#endif

#if !HAVE_WACCESS

int LIBNETXMS_EXPORTABLE waccess(const WCHAR *_path, int mode)
{
   char path[MAX_PATH];
   WideCharToMultiByteSysLocale(_path, path, sizeof(path));
   return access(path, mode);
}

#endif

#if !HAVE_WMKDIR

int LIBNETXMS_EXPORTABLE wmkdir(const WCHAR *_path, int mode)
{
   char path[MAX_PATH];
   WideCharToMultiByteSysLocale(_path, path, sizeof(path));
   return mkdir(path, mode);
}

#endif

#if !HAVE_WUTIME

int LIBNETXMS_EXPORTABLE wutime(const WCHAR *_path, utimbuf *buf)
{
   char path[MAX_PATH];
   WideCharToMultiByteSysLocale(_path, path, sizeof(path));
   return utime(path, buf);
}

#endif

#if !HAVE_WGETENV

WCHAR LIBNETXMS_EXPORTABLE *wgetenv(const WCHAR *_string)
{
   static WCHAR value[8192];

   char name[256];
   WideCharToMultiByteSysLocale(_string, name, sizeof(name));
   char *p = getenv(name);
   if (p == nullptr)
      return nullptr;

   MultiByteToWideCharSysLocale(p, value, 8192);
   return value;
}

#endif

#if !HAVE_WCTIME

WCHAR LIBNETXMS_EXPORTABLE *wctime(const time_t *timep)
{
   static WCHAR value[256];
   MultiByteToWideCharSysLocale(ctime(timep), value, 256);
   return value;
}

#endif /* HAVE_WCTIME */

#if !HAVE_PUTWS

int LIBNETXMS_EXPORTABLE putws(const WCHAR *s)
{
#if HAVE_FPUTWS
   fputws(s, stdout);
   putwc(L'\n', stdout);
#else
   printf("%S\n", s);
#endif /* HAVE_FPUTWS */
   return 1;
}

#endif /* !HAVE_PUTWS */

#if !HAVE_WCSERROR && (HAVE_STRERROR || HAVE_DECL_STRERROR)

WCHAR LIBNETXMS_EXPORTABLE *wcserror(int errnum)
{
   static thread_local WCHAR value[256];
#if HAVE_STRERROR_R
   char buffer[256];
#if HAVE_POSIX_STRERROR_R
   strerror_r(errnum, buffer, 256);
   MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, buffer, -1, value, 256);
#else
   MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, strerror_r(errnum, buffer, 256), -1, value, 256);
#endif
#else
   MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, strerror(errnum), -1, value, 256);
#endif
   return value;
}

#endif /* !HAVE_WCSERROR && HAVE_STRERROR */

#if !HAVE_WCSERROR_R && HAVE_STRERROR_R

#if HAVE_POSIX_STRERROR_R
int LIBNETXMS_EXPORTABLE wcserror_r(int errnum, WCHAR *strerrbuf, size_t buflen)
#else
WCHAR LIBNETXMS_EXPORTABLE *wcserror_r(int errnum, WCHAR *strerrbuf, size_t buflen)
#endif /* HAVE_POSIX_STRERROR_R */
{
   char *mbbuf;
#if HAVE_POSIX_STRERROR_R
   int err = 0;
#endif /* HAVE_POSIX_STRERROR_R */

   mbbuf = (char *)MemAlloc(buflen);
   if (mbbuf != NULL)
   {
#if HAVE_POSIX_STRERROR_R
      err = strerror_r(errnum, mbbuf, buflen);
      MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, mbbuf, -1, strerrbuf, buflen);
#else
      MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, strerror_r(errnum, mbbuf, buflen), -1, strerrbuf, buflen);
#endif
      MemFree(mbbuf);
   }
   else
   {
      *strerrbuf = 0;
   }

#if HAVE_POSIX_STRERROR_R
   return err;
#else
   return strerrbuf;
#endif
}

#endif /* !HAVE_WCSERROR_R && HAVE_STRERROR_R */

/*************************************************************************
 * Wrappers for wprintf/wscanf family
 *
 * All these wrappers replaces %s with %S and %c with %C
 * because in POSIX version of wprintf functions %s and %c
 * means "multibyte string/char" instead of expected "UNICODE string/char"
 *************************************************************************/

/**
 * Internal version of wprintf() with Windows compatible format specifiers
 */
int LIBNETXMS_EXPORTABLE nx_wprintf(const WCHAR *format, ...)
{
   va_list args;
   int rc;

   va_start(args, format);
   rc = nx_vwprintf(format, args);
   va_end(args);
   return rc;
}

/**
 * Internal version of fwprintf() with Windows compatible format specifiers
 */
int LIBNETXMS_EXPORTABLE nx_fwprintf(FILE *fp, const WCHAR *format, ...)
{
   va_list args;
   int rc;

   va_start(args, format);
   rc = nx_vfwprintf(fp, format, args);
   va_end(args);
   return rc;
}

/**
 * Internal version of swprintf() with Windows compatible format specifiers
 */
int LIBNETXMS_EXPORTABLE nx_swprintf(WCHAR *buffer, size_t size, const WCHAR *format, ...)
{
   va_list args;
   int rc;

   va_start(args, format);
   rc = nx_vswprintf(buffer, size, format, args);
   va_end(args);
   return rc;
}

/**
 * Internal version of wscanf() with Windows compatible format specifiers
 */
int LIBNETXMS_EXPORTABLE nx_wscanf(const WCHAR *format, ...)
{
   va_list args;
   int rc;

   va_start(args, format);
   rc = nx_vwscanf(format, args);
   va_end(args);
   return rc;
}

/**
 * Internal version of fwscanf() with Windows compatible format specifiers
 */
int LIBNETXMS_EXPORTABLE nx_fwscanf(FILE *fp, const WCHAR *format, ...)
{
   va_list args;
   int rc;

   va_start(args, format);
   rc = nx_vfwscanf(fp, format, args);
   va_end(args);
   return rc;
}

/**
 * Internal version of swscanf() with Windows compatible format specifiers
 */
int LIBNETXMS_EXPORTABLE nx_swscanf(const WCHAR *str, const WCHAR *format, ...)
{
   va_list args;
   int rc;

   va_start(args, format);
   rc = nx_vswscanf(str, format, args);
   va_end(args);
   return rc;
}

/**
 * Length of local format buffer
 */
#define LOCAL_FORMAT_BUFFER_LENGTH  256

/**
 * Replace string/char format specifiers from Windows to POSIX version
 */
static WCHAR *ReplaceFormatSpecs(const WCHAR *oldFormat, WCHAR *localBuffer)
{
   size_t len = wcslen(oldFormat) + 1;
   WCHAR *fmt = (len <= LOCAL_FORMAT_BUFFER_LENGTH) ? localBuffer : static_cast<WCHAR*>(MemAlloc(len * sizeof(WCHAR)));
   memcpy(fmt, oldFormat, len * sizeof(WCHAR));

   int state = 0;
   bool hmod;
   for(WCHAR *p = fmt; *p != 0; p++)
   {
      switch (state)
      {
         case 0:	// Normal text
            if (*p == L'%')
            {
               state = 1;
               hmod = false;
            }
            break;
         case 1:	// Format start
            switch (*p)
            {
               case L's':
                  if (hmod)
                  {
                     memmove(p - 1, p, wcslen(p - 1) * sizeof(TCHAR));
                  }
                  else
                  {
                     *p = L'S';
                  }
                  state = 0;
                  break;
               case L'S':
                  *p = L's';
                  state = 0;
                  break;
               case L'c':
                  if (hmod)
                  {
                     memmove(p - 1, p, wcslen(p - 1) * sizeof(TCHAR));
                  }
                  else
                  {
                     *p = L'C';
                  }
                  state = 0;
                  break;
               case L'C':
                  *p = L'c';
                  state = 0;
                  break;
               case L'.':	// All this characters could be part of format specifier
               case L'*':	// and has no interest for us
               case L'+':
               case L'-':
               case L' ':
               case L'#':
               case L'0':
               case L'1':
               case L'2':
               case L'3':
               case L'4':
               case L'5':
               case L'6':
               case L'7':
               case L'8':
               case L'9':
               case L'l':
               case L'L':
               case L'F':
               case L'N':
               case L'w':
                  break;
               case L'h':	// check for %hs
                  hmod = true;
                  break;
               default:		// All other characters means end of format
                  state = 0;
                  break;

            }
            break;
      }
   }
   return fmt;
}

/**
 * Free format buffer of it is not local
 */
#define FreeFormatBuffer(b) do { if (b != localBuffer) MemFree(b); } while(0)

/**
 * Internal version of vwprintf() with Windows compatible format specifiers
 */
int LIBNETXMS_EXPORTABLE nx_vwprintf(const WCHAR *format, va_list args)
{
   WCHAR localBuffer[LOCAL_FORMAT_BUFFER_LENGTH];
   WCHAR *fmt = ReplaceFormatSpecs(format, localBuffer);
   int rc = vwprintf(fmt, args);
   FreeFormatBuffer(fmt);
   return rc;
}

/**
 * Internal version of vfwprintf() with Windows compatible format specifiers
 */
int LIBNETXMS_EXPORTABLE nx_vfwprintf(FILE *fp, const WCHAR *format, va_list args)
{
   WCHAR localBuffer[LOCAL_FORMAT_BUFFER_LENGTH];
   WCHAR *fmt = ReplaceFormatSpecs(format, localBuffer);
   int rc = vfwprintf(fp, fmt, args);
   FreeFormatBuffer(fmt);
   return rc;
}

/**
 * Internal version of vswprintf() with Windows compatible format specifiers
 */
int LIBNETXMS_EXPORTABLE nx_vswprintf(WCHAR *buffer, size_t size, const WCHAR *format, va_list args)
{
   WCHAR localBuffer[LOCAL_FORMAT_BUFFER_LENGTH];
   WCHAR *fmt = ReplaceFormatSpecs(format, localBuffer);
   int rc = vswprintf(buffer, size, fmt, args);
   FreeFormatBuffer(fmt);
   return rc;
}

/**
 * Internal version of vwscanf() with Windows compatible format specifiers
 */
int LIBNETXMS_EXPORTABLE nx_vwscanf(const WCHAR *format, va_list args)
{
   WCHAR localBuffer[LOCAL_FORMAT_BUFFER_LENGTH];
   WCHAR *fmt = ReplaceFormatSpecs(format, localBuffer);
#if HAVE_VWSCANF
   int rc = vwscanf(fmt, args);
#else
   int rc = -1; // FIXME: add workaround implementation
#endif
   FreeFormatBuffer(fmt);
   return rc;
}

/**
 * Internal version of vfwscanf() with Windows compatible format specifiers
 */
int LIBNETXMS_EXPORTABLE nx_vfwscanf(FILE *fp, const WCHAR *format, va_list args)
{
   WCHAR localBuffer[LOCAL_FORMAT_BUFFER_LENGTH];
   WCHAR *fmt = ReplaceFormatSpecs(format, localBuffer);
#if HAVE_VFWSCANF
   int rc = vfwscanf(fp, fmt, args);
#else
   int rc = -1; // FIXME: add workaround implementation
#endif
   FreeFormatBuffer(fmt);
   return rc;
}

/**
 * Internal version of vswscanf() with Windows compatible format specifiers
 */
int LIBNETXMS_EXPORTABLE nx_vswscanf(const WCHAR *str, const WCHAR *format, va_list args)
{
   WCHAR localBuffer[LOCAL_FORMAT_BUFFER_LENGTH];
   WCHAR *fmt = ReplaceFormatSpecs(format, localBuffer);
#if HAVE_VSWSCANF
   int rc = vswscanf(str, fmt, args);
#else
   int rc = -1; // FIXME: add workaround implementation
#endif
   FreeFormatBuffer(fmt);
   return rc;
}

#endif /* defined(UNICODE) */

#endif /* !defined(_WIN32) */

/**
 * UNICODE version of inet_addr()
 */
UINT32 LIBNETXMS_EXPORTABLE inet_addr_w(const WCHAR *pszAddr)
{
   char szBuffer[256];
   WideCharToMultiByte(CP_ACP, WC_COMPOSITECHECK | WC_DEFAULTCHAR, pszAddr, -1, szBuffer, 256, NULL, NULL);
   return inet_addr(szBuffer);
}

#if defined(_WITH_ENCRYPTION) && WITH_OPENSSL

/**
 * Get OpenSSL error string as UNICODE string
 * Buffer must be at least 256 character long
 */
WCHAR LIBNETXMS_EXPORTABLE *ERR_error_string_W(int nError, WCHAR *pwszBuffer)
{
   char text[256];

   memset(text, 0, sizeof(text));
   ERR_error_string(nError, text);
   MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, text, -1, pwszBuffer, 256);
   return pwszBuffer;
}

#endif
