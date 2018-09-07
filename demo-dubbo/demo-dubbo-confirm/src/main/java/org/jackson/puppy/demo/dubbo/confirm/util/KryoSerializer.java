package org.jackson.puppy.demo.dubbo.confirm.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class KryoSerializer {

	private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
		Kryo kryo = new Kryo();
		kryo.setReferences(true);
		kryo.setRegistrationRequired(false);

		((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
				.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());

		return kryo;
	});

	public static byte[] serializer(Object obj) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (Output output = new Output(byteArrayOutputStream)) {
			Kryo kryo = KRYO_THREAD_LOCAL.get();
			kryo.writeObject(output, obj);
		}
		return byteArrayOutputStream.toByteArray();
	}

	public static <T> T deSerializer(byte[] byteArray, Class<T> type) {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
		Input input = new Input(byteArrayInputStream);
		Kryo kryo = KRYO_THREAD_LOCAL.get();
		return kryo.readObject(input, type);
	}
}