/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package ngram_tools;

import java.util.concurrent.atomic.AtomicBoolean;

import net.ikarus_systems.icarus.util.PropertyChangeSource;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public abstract class NGram extends PropertyChangeSource {
	
	private NGramState state = NGramState.BLANK;
	
	private Object lock = new Object();
	
	private final NGramDescriptor descriptor;
	
	private AtomicBoolean cancelled = new AtomicBoolean();
	
	protected NGram(NGramDescriptor descriptor) {
		if(descriptor==null)
			throw new IllegalArgumentException("Invalid descriptor"); //$NON-NLS-1$
		
		this.descriptor = descriptor;
	}

	final void setState(NGramState state) {
		NGramState oldValue;
		synchronized (lock) {
			oldValue = this.state;
			this.state = state;
		}
		
		firePropertyChange("state", oldValue, state); //$NON-NLS-1$
	}
	
	public final NGramState getState() {
		synchronized (lock) {
			return state;
		}
	}
	
	public final NGramDescriptor getDescriptor() {
		return descriptor;
	}
	
	public final NGramQuery getQuery() {
		return descriptor.getQuery();
	}
	
	public final boolean isCancelled() {
		return cancelled.get();
	}
	
	public final boolean isDone() {
		synchronized (lock) {
			return state==NGramState.FINISHED || state==NGramState.CANCELLED;
		}
	}
	
	/**
	 * Attempts to cancel this ngram by setting the internal {@code cancelled}
	 * flag to {@code true}.
	 * This method will throw an {@link IllegalArgumentException} if the
	 * ngram is not yet running or has already been finished or cancelled.
	 */
	public final void cancel() {
		synchronized (lock) {
			NGramState state = getState();
			if(state==NGramState.BLANK)
				throw new IllegalStateException("NGram generation not started yet!"); //$NON-NLS-1$
			if(state!=NGramState.RUNNING)
				throw new IllegalStateException("NGram generator not running!"); //$NON-NLS-1$
			if(!cancelled.compareAndSet(false, true))
				throw new IllegalStateException("NGram generator already cancelled!"); //$NON-NLS-1$
			
			setState(NGramState.CANCELLED);
		}
	}
	
	/**
	 * Runs the ngram and constructs the internal {@code NGramResult} object.
	 * Note that an implementation should regularly check for user originated
	 * cancellation by invoking {@link #isCancelled()}.
	 */
	public abstract void execute() throws Exception;
	
	/**
	 * Returns the (estimated) progress of the search in the range
	 * 0 to 100.
	 */
	public abstract int getProgress();
	
	/**
	 * Returns the result of this search operation.
	 * Note that not all implementations are required to support live
	 * previews of the final result. In this case they may decide to
	 * return {@code null} until the search has finished or an empty result.
	 */
	public abstract NGramResult getResult();
}