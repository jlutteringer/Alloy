package alloy.utilities.core;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by jlutteringer on 1/16/18.
 */
public abstract class SingleEntryIterator<T> implements Iterator<T> {
	private boolean nextGenerated = false;
	private T next;

	@Override
	public boolean hasNext() {
		if (!nextGenerated) {
			try {
				next = generateNext();
				nextGenerated = true;
			} catch (NoSuchElementException e) {
				nextGenerated = false;
			}
		}
		return nextGenerated;
	}

	@Override
	public T next() {
		if (nextGenerated) {
			nextGenerated = false;
			return next;
		}
		else {
			return generateNext();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	protected abstract T generateNext() throws NoSuchElementException;
}