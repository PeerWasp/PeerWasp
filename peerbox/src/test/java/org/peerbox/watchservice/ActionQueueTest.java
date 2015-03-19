package org.peerbox.watchservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.watchservice.filetree.composite.FileComponent;

public class ActionQueueTest {

	private FileComponentQueue queue;

	@Before
	public void setUp() throws Exception {
		queue = new FileComponentQueue();
	}

	@After
	public void tearDown() throws Exception {
		queue = null;
	}

	@Test
	public void testAddAndTake() throws InterruptedException {
		FileComponent f = createFileComponent();
		queue.add(f);
		FileComponent take = queue.take();
		assertNotNull(take);
		assertEquals(f, take);
	}

	@Test
	public void testAddAndTake_Order() throws InterruptedException {
		List<FileComponent> elements = new ArrayList<>();
		for (int i = 0; i < 10; ++i) {
			FileComponent f = createFileComponent();
			elements.add(f);
			if (i > 0) {
				assertTrue(
					elements.get(i).getAction().getTimestamp() >
					elements.get(i - 1).getAction().getTimestamp());
			}
			Thread.sleep(100); // need different timestamps!
		}

		// random shuffle of elements and add to queue
		List<FileComponent> shuffled = new ArrayList<>(elements);
		Collections.shuffle(shuffled);
		for (FileComponent f : shuffled) {
			queue.add(f);
		}
		assertTrue(queue.size() == 10);

		// check that we take ordered (oldest first)
		int index = 0;
		while (queue.size() > 0) {
			FileComponent next = queue.take();
			assertNotNull(next);
			assertEquals(next, elements.get(index));
			++index;
		}
	}

	@Test
	public void testRemove() throws InterruptedException {
		// only one is in queue
		FileComponent f1 = createFileComponent();
		FileComponent f2 = createFileComponent();
		queue.add(f1);
		assertTrue(queue.size() == 1);

		// remove element NOT in queue
		queue.remove(f2);
		assertTrue(queue.size() == 1);

		// remove element in queue
		queue.remove(f1);
		assertTrue(queue.size() == 0);
	}

	@Test
	public void testSize() throws InterruptedException {
		FileComponent f;
		assertTrue(queue.size() == 0);
		f = createFileComponent();
		queue.add(f);
		assertTrue(queue.size() == 1);
		f = createFileComponent();
		queue.add(f);
		assertTrue(queue.size() == 2);
		queue.remove(f);
		assertTrue(queue.size() == 1);
	}


	/**
	 * @return mocked file component
	 */
	private FileComponent createFileComponent() {
		FileComponent f = Mockito.mock(FileComponent.class);
		Mockito.stub(f.getAction()).toReturn(new Action());
		return f;
	}
}
