package picoded.servlet.api.exception;

public class HaltException extends RuntimeException {

  public HaltException() {
    super();
  }

  public HaltException(String s){
    super(s);
  }
}
