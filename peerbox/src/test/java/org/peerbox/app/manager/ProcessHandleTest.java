package org.peerbox.app.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.Future;

import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.BaseJUnitTest;

public class ProcessHandleTest extends BaseJUnitTest {

	private IProcessComponent<Void> process;
	private ProcessHandle<Void> handle;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		process = Mockito.mock(IProcessComponent.class);
		handle = new ProcessHandle<Void>(process);

		Mockito.stub(process.executeAsync()).toReturn(Mockito.mock(Future.class));
	}

	@After
	public void tearDown() throws Exception {
		process = null;
		handle = null;
	}

	@Test
	public void testProcessHandle() {
		assertNull(handle.getFuture());
		assertNotNull(handle.getProcess());
		assertEquals(process, handle.getProcess());
		Mockito.verifyZeroInteractions(process);
	}

	@Test
	public void testGetFuture() throws InvalidProcessStateException, ProcessExecutionException {
		assertNull(handle.getFuture());

		handle.executeAsync();

		assertNotNull(handle.getFuture());
	}

	@Test
	public void testExecute() throws InvalidProcessStateException, ProcessExecutionException {
		handle.execute();
		Mockito.verify(process, Mockito.times(1)).execute();
		Mockito.verifyNoMoreInteractions(process);
	}

	@Test
	public void testExecuteAsync() throws InvalidProcessStateException, ProcessExecutionException {
		handle.executeAsync();
		Mockito.verify(process, Mockito.times(1)).executeAsync();
		Mockito.verifyNoMoreInteractions(process);
	}

}
