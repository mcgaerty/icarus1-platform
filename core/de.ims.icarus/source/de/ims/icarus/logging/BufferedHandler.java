/*
 * $Revision: 29 $
 * $Date: 2013-05-03 20:03:21 +0200 (Fr, 03 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/logging/BufferedHandler.java $
 *
 * $LastChangedDate: 2013-05-03 20:03:21 +0200 (Fr, 03 Mai 2013) $ 
 * $LastChangedRevision: 29 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.logging;

import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.events.WeakEventSource;


/**
 * @author Markus Gärtner
 * @version $Id: BufferedHandler.java 29 2013-05-03 18:03:21Z mcgaerty $
 *
 */
public class BufferedHandler extends Handler {
	
	private LogRecord[] buffer;
	private int startOffset = 0;
	private int size = 0;
	
	private static final int MAX_RECORDS = 3200;
	private static final int START_RECORDS = 100;
	
	private WeakEventSource eventSource;

	public BufferedHandler() {
		// no-op
	}
	
	public void fireEvent(EventObject event) {
		if(eventSource==null) {
			eventSource = new WeakEventSource(this);
		}
		eventSource.fireEvent(event);
	}

	/**
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	@Override
	public synchronized void publish(LogRecord record) {
		if(buffer==null) {
			buffer = new LogRecord[START_RECORDS];
		}
		
		// Get offset based on relative start index
		int index = startOffset + size;
		// FIXME seems not to grow?
		
		// Expand buffer if required
		if(index >= buffer.length && buffer.length < MAX_RECORDS) {
			int newSize = Math.min(buffer.length*2, MAX_RECORDS);
			LogRecord[] newBuffer = new LogRecord[newSize];
			System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
		}
		
		// Get real index
		if(index >= buffer.length) {
			index -= buffer.length;
		}
		
		LogRecord removed = buffer[index];
		
		buffer[index] = record;
		
		// Shift start offset if required
		if(index == startOffset && size!=0) {
			startOffset++;
			if(startOffset >= buffer.length) {
				startOffset -= buffer.length;
			}
		} else {
			// Size only increases if we do not have to shift the start
			// index (i.e. remove the oldest record)
			size++;
		}
		
		fireEvent(new EventObject(Events.ADDED, 
				"record", record, "index", index)); //$NON-NLS-1$ //$NON-NLS-2$
		
		if(removed!=null) {
			fireEvent(new EventObject(Events.REMOVED, 
					"record", removed, "index", index)); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @see java.util.logging.Handler#flush()
	 */
	@Override
	public void flush() {
		// no-op
	}
	
	public synchronized void clear() {
		if(buffer==null) {
			return;
		}
		
		startOffset = 0;
		size = 0;
		Arrays.fill(buffer, null);
		
		fireEvent(new EventObject(Events.CLEANED));
	}

	/**
	 * @see java.util.logging.Handler#close()
	 */
	@Override
	public void close() throws SecurityException {
		clear();
	}

	public synchronized int getRecordCount() {
		return size;
	}
	
	public synchronized LogRecord getRecord(int index) {
		if(buffer==null) {
			return null;
		}
		
		if(index>=size) {
			//return null;
			throw new IndexOutOfBoundsException("Invalid index: "+index); //$NON-NLS-1$
		}
		
		int offset = startOffset + index;
		if(offset >= buffer.length) {
			offset -= buffer.length;
		}
		
		return buffer[offset];
	}
	
	public synchronized LogRecord getLatestRecord() {
		return size<=0 ? null : getRecord(size-1);
	}

	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}

	public void removeListener(EventListener listener) {
		eventSource.removeListener(listener);
	}
	
}