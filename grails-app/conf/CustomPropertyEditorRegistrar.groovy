
import FloatPropertyEditor;

import org.springframework.beans.PropertyEditorRegistrar
import org.springframework.beans.PropertyEditorRegistry;


class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar {

	@Override
	public void registerCustomEditors(PropertyEditorRegistry reg) {
		reg.registerCustomEditor(Float.class, new FloatPropertyEditor())		
	}

}
