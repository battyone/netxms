/*
 ** NetXMS - Network Management System
 ** NetXMS Foundation Library
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
 ** File: bytestream.cpp
 **
 **/

#include "libnetxms.h"

/**
 * Create empty byte stream
 */
ByteStream::ByteStream(size_t initial)
{
   m_allocated = initial;
   m_size = 0;
   m_pos = 0;
   m_allocationStep = 4096;
   m_data = (m_allocated > 0) ? static_cast<BYTE*>(MemAlloc(m_allocated)) : nullptr;
}

/**
 * Create byte stream from existing data
 */
ByteStream::ByteStream(const void *data, size_t size)
{
   m_allocated = size;
   m_size = size;
   m_pos = 0;
   m_allocationStep = 4096;
   m_data = (m_allocated > 0) ? static_cast<BYTE*>(MemCopyBlock(data, size)) : nullptr;
}

/**
 * Destructor
 */
ByteStream::~ByteStream()
{
   MemFree(m_data);
}

/**
 * Take byte stream buffer (byte stream will became empty)
 */
BYTE *ByteStream::takeBuffer()
{
   BYTE *data = m_data;
   m_data = nullptr;
   m_allocated = 0;
   m_size = 0;
   m_pos = 0;
   return data;
}

/**
 * Write data
 */
void ByteStream::write(const void *data, size_t size)
{
   if (m_pos + size > m_allocated)
   {
      m_allocated += std::max(size, m_allocationStep);
      m_data = MemRealloc(m_data, m_allocated);
   }
   memcpy(&m_data[m_pos], data, size);
   m_pos += size;
   if (m_pos > m_size)
      m_size = m_pos;
}

/**
 * Write string in UTF-8 prepended with length
 */
void ByteStream::writeString(const TCHAR *s)
{
#ifdef UNICODE
   char *utf8str = UTF8StringFromWideString(s);
#else
   char *utf8str = UTF8StringFromMBString(s);
#endif
   writeStringUtf8(utf8str);
   MemFree(utf8str);
}

/**
 * Write string in UTF-8 prepended with length
 */
void ByteStream::writeStringUtf8(const char *s)
{
   // write len < 2^15 as 2 bytes and 4 bytes with higher bit set otherwise
   uint32_t len = (UINT32)strlen(s);
   if (len < 0x8000)
      write((UINT16)len);
   else
      write(len | 0x80000000);

   write(s, len);
}

/**
 * Read data
 */
size_t ByteStream::read(void *buffer, size_t count)
{
   size_t c = std::min(count, m_size - m_pos);
   if (c > 0)
   {
      memcpy(buffer, &m_data[m_pos], c);
      m_pos += c;
   }
   return c;
}

/**
 * Read 16 bit integer
 */
INT16 ByteStream::readInt16()
{
   if (m_size - m_pos < 2)
   {
      m_pos = m_size;
      return 0;
   }

   UINT16 n;
   memcpy(&n, &m_data[m_pos], 2);
   m_pos += 2;
   return (INT16)ntohs(n);
}

/**
 * Read unsigned 16 bit integer
 */
UINT16 ByteStream::readUInt16()
{
   if (m_size - m_pos < 2)
   {
      m_pos = m_size;
      return 0;
   }

   UINT16 n;
   memcpy(&n, &m_data[m_pos], 2);
   m_pos += 2;
   return ntohs(n);
}

/**
 * Read 32 bit integer
 */
INT32 ByteStream::readInt32()
{
   if (m_size - m_pos < 4)
   {
      m_pos = m_size;
      return 0;
   }

   UINT32 n;
   memcpy(&n, &m_data[m_pos], 4);
   m_pos += 4;
   return (INT32)ntohl(n);
}

/**
 * Read unsigned 32 bit integer
 */
UINT32 ByteStream::readUInt32()
{
   if (m_size - m_pos < 4)
   {
      m_pos = m_size;
      return 0;
   }

   UINT32 n;
   memcpy(&n, &m_data[m_pos], 4);
   m_pos += 4;
   return ntohl(n);
}

/**
 * Read 64 bit integer
 */
INT64 ByteStream::readInt64()
{
   if (m_size - m_pos < 8)
   {
      m_pos = m_size;
      return 0;
   }

   UINT64 n;
   memcpy(&n, &m_data[m_pos], 8);
   m_pos += 8;
   return (INT64)ntohq(n);
}

/**
 * Read unsigned 64 bit integer
 */
UINT64 ByteStream::readUInt64()
{
   if (m_size - m_pos < 8)
   {
      m_pos = m_size;
      return 0;
   }

   UINT64 n;
   memcpy(&n, &m_data[m_pos], 8);
   m_pos += 8;
   return ntohq(n);
}

/**
 * Read double
 */
double ByteStream::readDouble()
{
   if (m_size - m_pos < 8)
   {
      m_pos = m_size;
      return 0;
   }

   double n;
   memcpy(&n, &m_data[m_pos], 8);
   m_pos += 8;
   return ntohd(n);
}

/**
 * Read UTF-8 encoded string. Returned string is dynamically allocated and must be freed by caller.
 */
TCHAR *ByteStream::readString()
{
   if (m_size - m_pos < 2)
      return NULL;

   BYTE b = readByte();
   m_pos--;
   size_t len;
   if (b & 0x80)
   {
      // 4 byte length
      if (m_size - m_pos < 4)
         return NULL;
      len = (size_t)(readUInt32() & ~0x80000000);
   }
   else
   {
      len = (size_t)readInt16();
   }

   if (m_size - m_pos < len)
      return NULL;

   TCHAR *s = MemAllocString(len + 1);
#ifdef UNICODE
   utf8_to_wchar(reinterpret_cast<char*>(&m_data[m_pos]), len, s, len + 1);
#else
   utf8_to_mb(reinterpret_cast<char*>(&m_data[m_pos]), len, s, len + 1);
#endif
   s[len] = 0;
   m_pos += len;
   return s;
}

/**
 * Read UTF-8 encoded string. Returned string is dynamically allocated and must be freed by caller.
 */
char *ByteStream::readStringUtf8()
{
   if (m_size - m_pos < 2)
      return NULL;

   BYTE b = readByte();
   m_pos--;
   size_t len;
   if (b & 0x80)
   {
      // 4 byte length
      if (m_size - m_pos < 4)
         return NULL;
      len = (size_t)(readUInt32() & ~0x80000000);
   }
   else
   {
      len = (size_t)readInt16();
   }

   if (m_size - m_pos < len)
      return NULL;

   char *s = (char *)malloc(len + 1);
   memcpy(s, &m_data[m_pos], len);
   s[len] = 0;
   m_pos += len;
   return s;
}

/**
 * Save byte stream to file
 */
bool ByteStream::save(int f)
{
#ifdef _WIN32
   return _write(f, m_data, (unsigned int)m_size) == (unsigned int)m_size;
#else
   return ::write(f, m_data, (int)m_size) == (int)m_size;
#endif
}

/**
 * Load from file
 */
ByteStream *ByteStream::load(const TCHAR *file)
{
   size_t size;
   BYTE *data = LoadFile(file, &size);
   if (data == NULL)
      return NULL;
   ByteStream *s = new ByteStream(0);
   s->m_allocated = size;
   s->m_size = size;
   s->m_data = data;
   return s;
}
