package wolfgang.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Utils {
	public interface Predicate<T> {
		boolean apply(T type);
	}
	
	public static <X> Collection<X> filter(Collection<X> target, Predicate<X> predicate) {
		Collection<X> result = new ArrayList<X>();
		for(X element : target)
			if(predicate.apply(element))
				result.add(element);
		return result;
	}
	
	public interface Function0<A> { // function A
		public A apply();
	}
	
	public interface Function1<A,B> { // function A -> B
		public B apply(A x);
	}
	
	public interface Function2<A,B,C> { // function A -> B -> C
		public C apply(A x, B y);
	}
	
	public static <X,Y> Collection<Y> map2(Function1<X,Y> fun, Collection<X> xs) {
		Collection<Y> ret = new ArrayList<Y>(xs.size());
		for(X x : xs)
			ret.add(fun.apply(x));
		return ret;
	}
	
	public static <X> boolean any(Function1<X,Boolean> fun, Collection<X> xs) {
		for(X x : xs)
			if(Boolean.TRUE.equals(fun.apply(x)))
				return Boolean.TRUE;
		return Boolean.FALSE;
	}
	
	public static <X> boolean all(Function1<X,Boolean> fun, Collection<X> xs) {
		for(X x : xs)
			if(Boolean.FALSE.equals(fun.apply(x)))
				return Boolean.FALSE;
		return Boolean.TRUE;
	}
	
	public static <X,Y> X reduce(Function2<X,Y,X> fun, X init, Collection<Y> ys) {
		for(Y y : ys)
			init = fun.apply(init, y);
		return init;
	}
	
	public static <X> Collection<X> reverse_c(Collection<X> xs) {
		List<X> ret = new ArrayList<X>(xs);
		Collections.reverse(ret);
		return ret;
	}
	
	public static <X> Collection<X> reverse_t(X[] xs) {
		List<X> ret = new ArrayList<>(xs.length);
		Collections.reverse(ret);
		return ret;
	}
	
	public static <X> Collection<X> take(int n, Collection<X> xs) {
		ArrayList<X> ret = new ArrayList<X>(n<xs.size()?n:xs.size());
		for(X x : xs) {
			if(n-- == 0) break;
			ret.add(x);
		}
		return ret;
	}

	public static <X,Y> Y max(Collection<X> xs, Function2<Y,X,Y> fun) {
		Y m = null;
		for(X x : xs)
			m = fun.apply(m,x);
		return m;		
	}
	
	public static <X> void evalAll(Collection<Function0<X>> xs) {
		for(Function0<X> x : xs)
			x.apply();
	}
}
