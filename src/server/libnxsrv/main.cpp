/* 
** NetXMS - Network Management System
** Server Library
** Copyright (C) 2003, 2004, 2005, 2006, 2007 Victor Kirhenshtein
**
** This program is free software; you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation; either version 2 of the License, or
** (at your option) any later version.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with this program; if not, write to the Free Software
** Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
**
** File: main.cpp
**
**/

#include "libnxsrv.h"


//
// Global variables
//

DWORD LIBNXSRV_EXPORTABLE g_dwFlags = AF_USE_SYSLOG;
int LIBNXSRV_EXPORTABLE g_nDebugLevel = 0;


//
// Agent result codes
//

static struct
{
   int iCode;
   const TCHAR *pszText;
} m_agentErrors[] =
{
   { ERR_SUCCESS, _T("Success") },
   { ERR_UNKNOWN_COMMAND, _T("Unknown command") },
   { ERR_AUTH_REQUIRED, _T("Authentication required") },
   { ERR_ACCESS_DENIED, _T("Access denied") },
   { ERR_UNKNOWN_PARAMETER, _T("Unknown parameter") },
   { ERR_REQUEST_TIMEOUT, _T("Request timeout") },
   { ERR_AUTH_FAILED, _T("Authentication failed") },
   { ERR_ALREADY_AUTHENTICATED, _T("Already authenticated") },
   { ERR_AUTH_NOT_REQUIRED, _T("Authentication not required") },
   { ERR_INTERNAL_ERROR, _T("Internal error") },
   { ERR_NOT_IMPLEMENTED, _T("Not implemented") },
   { ERR_OUT_OF_RESOURCES, _T("Out of resources") },
   { ERR_NOT_CONNECTED, _T("Not connected") },
   { ERR_CONNECTION_BROKEN, _T("Connection broken") },
   { ERR_BAD_RESPONSE, _T("Bad response") },
   { ERR_IO_FAILURE, _T("I/O failure") },
   { ERR_RESOURCE_BUSY, _T("Resource busy") },
   { ERR_EXEC_FAILED, _T("External program execution failed") },
   { ERR_ENCRYPTION_REQUIRED, _T("Encryption required") },
   { ERR_NO_CIPHERS, _T("No acceptable ciphers") },
   { ERR_INVALID_PUBLIC_KEY, _T("Invalid public key") },
   { ERR_INVALID_SESSION_KEY, _T("Invalid session key") },
   { ERR_CONNECT_FAILED, _T("Connect failed") },
   { -1, NULL }
};


//
// Resolve agent's error code to text
//

const TCHAR LIBNXSRV_EXPORTABLE *AgentErrorCodeToText(int iError)
{
   int i;

   for(i = 0; m_agentErrors[i].pszText != NULL; i++)
      if (iError == m_agentErrors[i].iCode)
         return m_agentErrors[i].pszText;
   return _T("Unknown error code");
}


//
// Destroy interface list created by discovery functions
//

void LIBNXSRV_EXPORTABLE DestroyInterfaceList(INTERFACE_LIST *pIfList)
{
   if (pIfList != NULL)
   {
      if (pIfList->pInterfaces != NULL)
         free(pIfList->pInterfaces);
      free(pIfList);
   }
}


//
// Destroy ARP cache created by discovery functions
//

void LIBNXSRV_EXPORTABLE DestroyArpCache(ARP_CACHE *pArpCache)
{
   if (pArpCache != NULL)
   {
      if (pArpCache->pEntries != NULL)
         free(pArpCache->pEntries);
      free(pArpCache);
   }
}


//
// Destroy routing table
//

void LIBNXSRV_EXPORTABLE DestroyRoutingTable(ROUTING_TABLE *pRT)
{
   if (pRT != NULL)
   {
      safe_free(pRT->pRoutes);
      free(pRT);
   }
}


//
// Route comparision callback
//

static int CompareRoutes(const void *p1, const void *p2)
{
   return -(COMPARE_NUMBERS(((ROUTE *)p1)->dwDestMask, ((ROUTE *)p2)->dwDestMask));
}


//
// Sort routing table (put most specific routes first)
//

void LIBNXSRV_EXPORTABLE SortRoutingTable(ROUTING_TABLE *pRT)
{
   qsort(pRT->pRoutes, pRT->iNumEntries, sizeof(ROUTE), CompareRoutes);
}


//
// Debug printf - write debug record to log if level is less or equal current debug level
//

void LIBNXSRV_EXPORTABLE DbgPrintf(int level, const TCHAR *format, ...)
{
   va_list args;
   TCHAR buffer[4096];

   if (level > g_nDebugLevel)
      return;     // Required application flag(s) not set

   va_start(args, format);
   _vsntprintf(buffer, 4096, format, args);
   va_end(args);
   nxlog_write(MSG_DEBUG, EVENTLOG_DEBUG_TYPE, "s", buffer);
}


//
// Log custom message (mostly used by modules)
//

void LIBNXSRV_EXPORTABLE WriteLogOther(WORD wType, const TCHAR *format, ...)
{
   va_list args;
   TCHAR buffer[4096];

   va_start(args, format);
   _vsntprintf(buffer, 4096, format, args);
   va_end(args);
   nxlog_write(MSG_OTHER, wType, "s", buffer);
}


//
// DLL entry point
//

#ifdef _WIN32

BOOL WINAPI DllMain(HINSTANCE hInstance, DWORD dwReason, LPVOID lpReserved)
{
   if (dwReason == DLL_PROCESS_ATTACH)
      DisableThreadLibraryCalls(hInstance);
   return TRUE;
}

#endif   /* _WIN32 */
