package wolfgang.utils;

import java.util.ArrayList;
import java.util.Collection;

public class Utils {
	public interface Predicate<T> {
		boolean apply(T type);
	}
	
	public static <T> Collection<T> filter(Collection<T> target, Predicate<T> predicate) {
		Collection<T> result = new ArrayList<T>();
		for(T element : target)
			if(predicate.apply(element))
				result.add(element);
		return result;
	}
	
	public interface Function2<A,B> { // function A -> B
		public B apply(A x);
	}
	
	public static <X,Y> Collection<Y> map2(Function2<X,Y> fun, Collection<X> xs) {
		Collection<Y> ret = new ArrayList<>(xs.size());
		for(X x : xs)
			ret.add(fun.apply(x));
		return ret;
	}

}
