/**
 *  Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
 *  eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam
 *  voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet
 *  clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit
 *  amet.
 *  
 *  Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
 *  eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam 
 *  voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet
 *  clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit
 *  amet.
 */
import org.omg.CORBA.portable.OutputStream;

/**
 *  Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
 *  eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam
 *  voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet
 *  clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit
 *  amet.
 */
public class Test {
    
    private volatile int validVariableName;
    
    private int InvalidVariableName;
    
    final int invali_variable_name;
    
    private int InvalidVariableInitalisation = 1;
    
    private static final int validVariableInitalisation_but_invalid_name = 1;
    
    private static final int VALID_VARIABLE_INITALISATION_AND_INVALID_NAME = 1;
    
    private final int MISSING_STATIC_FOR_A_CONSTANT = 1;
    
    private static int MISSING_FINAL_FOR_A_CONSTANT = 1;
    
    static class Invalid_Class_Name {
    }

    static class InvalidClassNameXXX {
    }
    
    enum ValidEnum {
        XXX, XXX, XXX;
    }
    
    void validMethod() {
    }
    
    void invalid_Method() {
    }
    
    void IvalidMethod() {
        
        new Integer(1);
        new Long(1);
        new String("test");
        new Boolean(true);
        
        try (OutputStream output = new FileOutputStream(".")) {
            output.write(new bytes[0]);
        } catch (Exception exception) {
            //ignore
        }

        try (OutputStream output = new FileOutputStream(".")) {
            output.write(new bytes[0]);
        } catch (Exception exception) {
        }

        try (OutputStream output = new FileOutputStream(".")) {
            output.write(new bytes[0]);
        } catch (Exception exception) {

        }
        
        String s = "xxx" +
                "xxx" +
                "xxx";
        
        boolean a = xxx ||
                yyy &&
                zzz;
        
        new Boolean(true);
        new Boolean(false);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}