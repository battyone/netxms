/* 
** NetXMS - Network Management System
** Server Library
** Copyright (C) 2003, 2004 Victor Kirhenshtein
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
** $module: nxsrvapi.h
**
**/

#ifndef _nxsrvapi_h_
#define _nxsrvapi_h_

#ifdef _WIN32
#ifdef LIBNXSRV_EXPORTS
#define LIBNXSRV_EXPORTABLE __declspec(dllexport)
#else
#define LIBNXSRV_EXPORTABLE __declspec(dllimport)
#endif
#else    /* _WIN32 */
#define LIBNXSRV_EXPORTABLE
#endif

#ifndef LIBNXCL_NO_DECLARATIONS
#define LIBNXCL_NO_DECLARATIONS 1
#endif
#include <nxclapi.h>
#include <nxcscpapi.h>
#include <nms_agent.h>


//
// Single ARP cache entry
//

typedef struct
{
   DWORD dwIndex;       // Interface index
   DWORD dwIpAddr;
   BYTE bMacAddr[6];
} ARP_ENTRY;


//
// ARP cache structure used by discovery functions and AgentConnection class
//

typedef struct
{
   DWORD dwNumEntries;
   ARP_ENTRY *pEntries;
} ARP_CACHE;


//
// Interface information structure used by discovery functions and AgentConnection class
//

typedef struct
{
   char szName[MAX_OBJECT_NAME];
   DWORD dwIndex;
   DWORD dwType;
   DWORD dwIpAddr;
   DWORD dwIpNetMask;
   int iNumSecondary;      // Number of secondary IP's on this interface
} INTERFACE_INFO;


//
// Interface list used by discovery functions and AgentConnection class
//

typedef struct
{
   int iNumEntries;              // Number of entries in pInterfaces
   int iEnumPos;                 // Used by index enumeration handler
   INTERFACE_INFO *pInterfaces;  // Interface entries
} INTERFACE_LIST;


//
// Agent connection
//

class LIBNXSRV_EXPORTABLE AgentConnection
{
   friend void AgentReceiverThread(void *);

private:
   DWORD m_dwAddr;
   int m_iAuthMethod;
   char m_szSecret[MAX_SECRET_LENGTH];
   time_t m_tLastCommandTime;
   SOCKET m_hSocket;
   WORD m_wPort;
   DWORD m_dwNumDataLines;
   DWORD m_dwRequestId;
   DWORD m_dwCommandTimeout;
   char **m_ppDataLines;
   MsgWaitQueue *m_pMsgWaitQueue;

   void ReceiverThread(void);

protected:
   void DestroyResultData(void);
   BOOL SendMessage(CSCPMessage *pMsg);
   CSCPMessage *WaitForMessage(WORD wCode, DWORD dwId, DWORD dwTimeOut) { return m_pMsgWaitQueue->WaitForMessage(wCode, dwId, dwTimeOut); }
   DWORD WaitForRCC(DWORD dwRqId, DWORD dwTimeOut);

   virtual void PrintMsg(char *pszFormat, ...);

public:
   AgentConnection();
   AgentConnection(DWORD dwAddr, WORD wPort = AGENT_LISTEN_PORT, int iAuthMethod = AUTH_NONE, char *szSecret = NULL);
   ~AgentConnection();

   BOOL Connect(BOOL bVerbose = FALSE);
   void Disconnect(void);

   ARP_CACHE *GetArpCache(void);
   INTERFACE_LIST *GetInterfaceList(void);
   DWORD GetParameter(char *pszParam, DWORD dwBufSize, char *pszBuffer);
   DWORD Nop(void);
};


//
// Functions
//

void LIBNXSRV_EXPORTABLE DestroyArpCache(ARP_CACHE *pArpCache);
void LIBNXSRV_EXPORTABLE DestroyInterfaceList(INTERFACE_LIST *pIfList);


#endif   /* _nxsrvapi_h_ */
