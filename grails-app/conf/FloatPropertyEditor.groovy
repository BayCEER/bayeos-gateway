
import java.beans.PropertyEditorSupport

class FloatPropertyEditor  extends PropertyEditorSupport{
			
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (text != null) {
			value = Float.valueOf(text.replace(',', '.'))
		} else {
			value = null
		} 				
	}
	
	
}
