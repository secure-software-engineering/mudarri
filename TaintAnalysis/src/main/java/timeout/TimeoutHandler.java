package timeout;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeoutHandler {

  private Set<Future> futures = new HashSet<>();

  public void killAllFutures() {
    for (Future future : futures) {
      if (future != null) future.cancel(true);
    }
  }

  public void runWithTimeout(final Runnable runnable, long timeout,
                             TimeUnit timeUnit) throws Exception, Error {
    runWithTimeout(() -> {
      runnable.run();
      return null;
    }, timeout, timeUnit);
  }

  private <T> T runWithTimeout(Callable<T> callable, long timeout,
                               TimeUnit timeUnit) throws Exception, Error {
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final Future<T> future = executor.submit(callable);
    futures.add(future);
    executor.shutdown(); // This does not cancel the already-scheduled task.
    try {
      return future.get(timeout, timeUnit);
    } catch (TimeoutException e) {
      future.cancel(true);
      throw e;
    } catch (ExecutionException e) {
      Throwable t = e.getCause();
      if (t instanceof Error) {
        throw (Error) t;
      } else if (t instanceof Exception) {
        throw (Exception) t;
      } else {
        throw new IllegalStateException(t);
      }
    }
  }
}
