package kikaha.undertow.http;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class KikahaException extends RuntimeException {

    public KikahaException(String message){
        super(message);
    }

    public static class UnsupportedMediaTypeException extends KikahaException {

        public UnsupportedMediaTypeException(String message){
            super(message);
        }
    }
}
