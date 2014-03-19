/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.language.model.chunks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import de.ims.icarus.io.IOUtil;

/**
 *
 * <h5>Metadata header</h5>
 *
 * Each channel used to store index data in will be marked by a 1 byte header that
 * indicates the type and format of contained data. Those informations are automatically
 * written and read prior to any operations final implementations of this class might perform.
 *
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractIndexAdapter implements ChunkIndexReader, ChunkIndexWriter {

	public static byte HEADER_FLAG_LONG_INDEX = (1 << 0);
	public static byte HEADER_FLAG_LONG_OFFSET = (1 << 1);
	public static byte HEADER_FLAG_ELEMENTS = (1 << 2);
	public static byte HEADER_FLAG_FILE_ID = (1 << 3);

	public static int HEADER_BYTE_SIZE = 1;

	public static boolean supportsFlag(byte header, byte flag) {
		return (header & flag) == flag;
	}

	private final ChunkIndex chunkIndex;

	private SeekableByteChannel channel;

	private final boolean readFileId;
	private final boolean readElements;
	// Elements are indexed with long values
	private final boolean largeIndex;
	// Byte addresses are indexed with long values
	private final boolean longIndex;

	// Size of one chunk in bytes
	private final int blockSize;
	// Size of one chunk in the internal data storage
	private final int fieldSize;
	// Maximum number of chunks that can be stored in the backing data
	private final long maxChunkCount;

	private final byte header;

	public static final long INT_LIMIT = (long) Integer.MAX_VALUE;
	public static final int BYTES_PER_INT = Integer.SIZE/8;
	public static final int BYTES_PER_LONG = Long.SIZE/8;

	protected AbstractIndexAdapter(ChunkIndex chunkIndex, boolean largeIndex, boolean longIndex) {
		if (chunkIndex == null)
			throw new NullPointerException("Invalid chunkIndex"); //$NON-NLS-1$
		if (chunkIndex.isLargeIndex() && !largeIndex)
			throw new IllegalArgumentException("Implementation unable to handle large indices"); //$NON-NLS-1$

		// Ensure we do not get fed compressed files
		for(int i=0; i<chunkIndex.getFileCount(); i++) {
			if(IOUtil.isGZipSource(chunkIndex.getFileAt(i)))
				throw new IllegalArgumentException("Cannot index gzip compressed files"); //$NON-NLS-1$
		}

		this.chunkIndex = chunkIndex;
		this.largeIndex = largeIndex;
		this.longIndex = longIndex;

		readFileId = chunkIndex.getFileCount()>1;
		readElements = chunkIndex.isMappingElements();

		int offsetFootprint = longIndex ? BYTES_PER_LONG : BYTES_PER_INT;
		int indexFootprint = largeIndex ? BYTES_PER_LONG : BYTES_PER_INT;

		// Calculate byte size of chunk blocks

		// 2 offset values for byte offsets
		int fieldSize = 2;
		int blockSize = 2 * offsetFootprint;
		if(isReadFileId()) {
			// Another 4 bytes for the file id
			fieldSize += 1;
			// File id is always integer based
			blockSize += BYTES_PER_INT;
		}
		if(isReadElements()) {
			// And yet another 2 index values for element indices
			fieldSize += 2;
			blockSize += 2 * indexFootprint;
		}

		this.blockSize = blockSize;
		this.fieldSize = fieldSize;
		maxChunkCount = ((largeIndex ? Long.MAX_VALUE : Integer.MAX_VALUE)/fieldSize) - 1;

		// Generate header byte
		byte header = 0;
		if(isReadFileId()) {
			header |= HEADER_FLAG_FILE_ID;
		}
		if(isReadElements()) {
			header |= HEADER_FLAG_ELEMENTS;
		}
		if(longIndex) {
			header |= HEADER_FLAG_LONG_OFFSET;
		}
		if(largeIndex) {
			header |= HEADER_FLAG_LONG_INDEX;
		}

		this.header = header;
	}

	/**
	 * @return the largeIndex
	 */
	public boolean isLargeIndex() {
		return largeIndex;
	}

	/**
	 * @return the longIndex
	 */
	public boolean isLongIndex() {
		return longIndex;
	}

	/**
	 * @return the blockSize
	 */
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * @return the fieldSize
	 */
	public int getFieldSize() {
		return fieldSize;
	}

	/**
	 * @return the maxChunkCount
	 */
	public long getMaxChunkCount() {
		return maxChunkCount;
	}

	/**
	 * @return the header
	 */
	public byte getHeader() {
		return header;
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexWriter#isIndexSupported(long)
	 */
	@Override
	public boolean isIndexSupported(long index) {
		return index<maxChunkCount;
	}

	protected void writeHeader(SeekableByteChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[]{header});
		channel.write(buffer);
	}

	protected void checkHeader(SeekableByteChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[1]);
		channel.position(0);
		channel.read(buffer);

		byte header = buffer.get(0);
		byte expected = getHeader();
		if(header != expected) {

			StringBuilder sb = new StringBuilder("Incompatible index data source - header corrupted: expected header ") //$NON-NLS-1$
			.append(Integer.toBinaryString(expected)).append(" - got ") //$NON-NLS-1$
			.append(Integer.toBinaryString(header))
			.append(" (long_index, long_offset, elements, file_id)"); //$NON-NLS-1$

			throw new IndexFormatException(sb.toString());
		}
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexReader#getChunkIndex()
	 */
	@Override
	public ChunkIndex getChunkIndex() {
		return chunkIndex;
	}

	public abstract boolean isOpen();

	protected abstract void delete();

	protected SeekableByteChannel getChannel() throws IOException {
		if(channel==null) {
			channel = getChunkIndex().openChannel();
		}
		return channel;
	}

	/**
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		if(!isOpen()) {
			return;
		}

		try {
			flush();
		} finally {
			delete();
		}
	}

	/**
	 * @see java.io.Flushable#flush()
	 */
	@Override
	public void flush() throws IOException {
		if(!isOpen())
			throw new IOException("Channel not open"); //$NON-NLS-1$

		writeData();
	}

	protected abstract void writeData() throws IOException;

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexWriter#clear()
	 */
	@Override
	public long clear() throws IOException {
		if(!isOpen())
			throw new IOException("Channel not open"); //$NON-NLS-1$

		long result = getChunkCount();

		if(channel!=null) {
			try {
				channel.truncate(0);
			} catch (IOException e) {
				delete();
			}
		}

		return result;
	}

	public boolean isReadFileId() {
		return readFileId;
	}

	public boolean isReadElements() {
		return readElements;
	}

	/**
	 * @see de.ims.icarus.language.model.chunks.ChunkIndexWriter#open()
	 */
	@Override
	public boolean open() throws IOException {
		if(isOpen()) {
			return false;
		}

		readData();

		return true;
	}

	protected abstract void readData() throws IOException;
}