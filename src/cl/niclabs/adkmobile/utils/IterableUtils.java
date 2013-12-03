package cl.niclabs.adkmobile.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class IterableUtils {
	private static class IteratorOfIterators<E> implements Iterator<E> {

		private final List<Iterator<? extends E>> iterators;
		private int index = 0;
		
		IteratorOfIterators(Iterator<? extends E> ... iterators) {
			this.iterators = Arrays.asList(iterators);
		}
		
		@Override
		public boolean hasNext() {
			return index < iterators.size() && iterators.get(index).hasNext();
		}

		@Override
		public E next() {
			if (index >= iterators.size() || !iterators.get(index).hasNext()) {
				throw new NoSuchElementException();
			}
			
			// Get the next element
			E element = iterators.get(index).next();
			
			// If we reached the end of the current iterator increase the index
			if (!iterators.get(index).hasNext()) {
				index++;
			}
			
			return element;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Operation not supported");
		}
		
	}
	
	/**
	 * Return a new iterator with the concatenation of the provided iterators
	 * @param iterators
	 * @return
	 */
	public static <E> Iterator<E> concatenateIterators(Class<E> cls, Iterator<? extends E> ... iterators) {
		return new IteratorOfIterators<E>(iterators);
	}
}
