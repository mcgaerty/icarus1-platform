/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.util.CompactProperties;
import net.ikarus_systems.icarus.util.PropertyChangeSource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class Search extends PropertyChangeSource {
	
	private SearchState state = SearchState.BLANK;
	
	private Object lock = new Object();
	
	private final Object target;
	private final SearchQuery query;
	
	private AtomicBoolean cancelled = new AtomicBoolean();
	
	private CompactProperties properties;
	
	private final SearchFactory factory;
	
	protected Search(SearchFactory factory, SearchQuery query, Object target) {
		if(factory==null)
			throw new IllegalArgumentException("Invalid factory"); //$NON-NLS-1$
		if(query==null)
			throw new IllegalArgumentException("Invalid query"); //$NON-NLS-1$
		if(target==null)
			throw new IllegalArgumentException("Invalid target"); //$NON-NLS-1$
		
		this.factory = factory;
		this.query = query;
		this.target = target;
	}

	final void setState(SearchState state) {
		SearchState oldValue;
		synchronized (lock) {
			oldValue = this.state;
			this.state = state;
		}
		
		firePropertyChange("state", oldValue, state); //$NON-NLS-1$
	}

	final void setState(SearchState expected, SearchState state) {
		SearchState oldValue;
		synchronized (lock) {
			oldValue = this.state;
			if(oldValue!=expected)
				throw new IllegalStateException();
			this.state = state;
		}
		
		firePropertyChange("state", oldValue, state); //$NON-NLS-1$
	}
	
	public final SearchState getState() {
		synchronized (lock) {
			return state;
		}
	}
	
	public final SearchFactory getFactory() {
		return factory;
	}
	
	public final SearchQuery getQuery() {
		return query;
	}
	
	public final Object getTarget() {
		return target;
	}
	
	public final Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}
	
	public final void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new CompactProperties();
		}
		
		properties.put(key, value);
	}
	
	public final boolean isCancelled() {
		return cancelled.get();
	}
	
	public final boolean isDone() {
		synchronized (lock) {
			return state==SearchState.DONE || state==SearchState.CANCELLED;
		}
	}
	
	public final boolean isRunning() {
		synchronized (lock) {
			return state==SearchState.RUNNING;
		}
	}
	
	/**
	 * Attempts to cancel this search by setting the internal {@code cancelled}
	 * flag to {@code true}.
	 * This method will throw an {@link IllegalArgumentException} if the
	 * search is not yet running or has already been finished or cancelled.
	 */
	public final void cancel() {
		synchronized (lock) {
			SearchState state = getState();
			if(state==SearchState.BLANK)
				throw new IllegalStateException("Search not started yet!"); //$NON-NLS-1$
			if(state!=SearchState.RUNNING)
				throw new IllegalStateException("Search not running!"); //$NON-NLS-1$
			if(!cancelled.compareAndSet(false, true))
				throw new IllegalStateException("Search already cancelled!"); //$NON-NLS-1$
			
			setState(SearchState.CANCELLED);
		}
	}
	
	public final void finish() {
		setState(SearchState.RUNNING, SearchState.DONE);
	}
	
	/**
	 * Runs the search and constructs the internal {@code SearchResult} object.
	 * Note that an implementation should regularly check for user originated
	 * cancellation by invoking {@link #isCancelled()}.
	 */
	public final void execute() throws Exception {
		if(isDone())
			throw new IllegalStateException("Cannot reuse search instance"); //$NON-NLS-1$
		
		setState(SearchState.BLANK, SearchState.RUNNING);
		
		if(!innerExecute() && isRunning()) {
			setState(SearchState.RUNNING, SearchState.DONE);
		}
	}

	/**
	 * Performs the implementation specific scheduling of
	 * the search operation. If an implementation realizes that
	 * the supplied data does not allow for a regular search
	 * execution to take place it can immediately return a 
	 * value of {@code false} to signal an early exit. The search
	 * will then set its state to {@value SearchState#DONE}.
	 * @return {@code true} if and only if the search operation
	 * was successfully scheduled.
	 */
	protected abstract boolean innerExecute() throws Exception;
	
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
	public abstract SearchResult getResult();
	
	public abstract SearchPerformanceInfo getPerformanceInfo();
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface SearchPerformanceInfo {
		
		// TODO add more performance fields!
		
		/**
		 * Performance field describing the total number of
		 * nodes that have been processed during a search operation.
		 * <p>
		 * A value of {@code -1} indicates that the search did not
		 * use "nodes" as objects in the process.
		 */
		public static final int VISITED_NODES = 1;

		/**
		 * Performance field describing the total number of
		 * edges that have been processed during a search operation.
		 * <p>
		 * A value of {@code -1} indicates that the search did not
		 * use "edges" as objects in the process.
		 */
		public static final int VISITED_EDGES = 2;

		/**
		 * Performance field describing the total number of constraint
		 * checks that were performed during the entire search.
		 */
		public static final int CHECKED_CONSTRAINTS = 3;

		/**
		 * Number of data items that passed the optional stage of
		 * pre-processing.
		 */
		public static final int PROCESSED_ITEMS = 4;

		/**
		 * Number of data items that were excluded by the pre-processing
		 * stage. Note that the sum of this value and {@link #PROCESSED_ITEMS}
		 * is exactly the number of items that was passed to the search 
		 * in the first place.
		 */
		public static final int IGNORED_ITEMS = 5;

		/**
		 * Number of data items declared to be valid matches prior to
		 * the optional filtering of the post-processing stage.
		 */
		public static final int MATCHED_ITEMS = 6;

		/**
		 * Number of data items that were considered to be matches
		 * and survived the optional post-processing stage.
		 */
		public static final int RETURNED_ITEMS = 7;
		
		/**
		 * Returns the exact time this search was executed
		 */
		Date getTime();
		
		/**
		 * Returns the amount of time in milliseconds this search
		 * required.
		 */
		long getDuration();
		
		/**
		 * Returns the stored value for the specified performance
		 * field. 
		 */
		long getPerformanceValue(int field);
		
		/**
		 * Returns the number of cores used by this search.
		 */
		int getCores();
	}
}
