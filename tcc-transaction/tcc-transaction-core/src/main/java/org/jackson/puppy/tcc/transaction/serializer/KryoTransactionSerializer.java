package org.jackson.puppy.tcc.transaction.serializer;

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
public class KryoTransactionSerializer<T> implements ObjectSerializer<T> {

	private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
		Kryo kryo = new Kryo();
		kryo.setReferences(true);
		kryo.setRegistrationRequired(false);
		//Fix the NPE bug when deserializing Collections.
		((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
				.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());

		return kryo;
	});

	public static Kryo getInstance() {
		return KRYO_THREAD_LOCAL.get();
	}

	public static <T> byte[] writeToByteArray(T obj) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Output output = new Output(byteArrayOutputStream);

		Kryo kryo = getInstance();
		kryo.writeClassAndObject(output, obj);
		output.flush();

		return byteArrayOutputStream.toByteArray();
	}

	public static <T> T readFromByteArray(byte[] byteArray) {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
		Input input = new Input(byteArrayInputStream);

		Kryo kryo = getInstance();
		return (T) kryo.readClassAndObject(input);
	}

	@Override
	public byte[] serialize(T t) {
		return writeToByteArray(t);
	}

	@Override
	public T deserialize(byte[] data) {
		return readFromByteArray(data);
	}
}