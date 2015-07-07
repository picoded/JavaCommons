package picoded.thread;


/// Extension of a common IO waiting pattern, such as thread.yield, etc.
/// used with a registered value, to "wait for". This can be extended to more complex conditions.
/// 
/// Its majority use case will however be the waitForEquals(V targetValue) call
///
/// The exact stretagy used in the lock handling can be described with the following
///
/// Note that when using AtomicValues, you will need to use notify(), to notify the changes when applicable.
/// Additionally, optimization is done for basic variable types (boolean, int, long)
/// 
/// ### BlockingWait
/// Uses thread locks, to detect and validate for changes
///
/// ### ThreadSleep
/// Uses a Thread.sleep(1), can be configured for a larger value
///
/// ### NanosSleep (default)
/// Uses LockSupport.parkNanos(1) instead of Thread.yield() / Thread.sleep(1), can be configured for a larger value
///
/// ### YieldingWait
/// Uses a looping Thread.yield()
///
/// ### SpinWait
/// Uses a spin thread, highly CPU intensive. Should not be used when threads > physical cores
///
/// ### SleepingWake
/// A gradual backing off waiting streatagy starting from SpinWait to NanosSleep
///
/// ### DynamicSleep
/// Uses a gradual backing off stretagy, extends SleepingWait, to ThreadSleep
/// 
/// ### DynamicBlock
/// Uses a gradual backing off streatagy, extends DynamicSleep, to BlockingWait
///
/// The thread blocking stretagy drew inspiration from the following: https://github.com/LMAX-Exchange/disruptor/wiki/Getting-Started
public class ConditionalValue<V> {
	
}
