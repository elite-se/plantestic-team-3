package plantuml.converter

import org.eclipse.xtext.conversion.impl.AbstractDeclarativeValueConverterService
import org.eclipse.xtext.conversion.IValueConverter
import org.eclipse.xtext.conversion.ValueConverter
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractNullSafeConverter

/**
 * This class converts specific inputs from rules to specific data types.
 */
class PumlValueConverter extends AbstractDeclarativeValueConverterService {
	
	/**
	 * Allows to create classes with strings containing empty spaces as names.
	 * @return The Value of the String containing no quotation marks.
	 */
	@ValueConverter(rule="STRING")
	def IValueConverter<String> STRING() {
		return new IValueConverter<String>() {
			override toString(String value) throws ValueConverterException {
				if (value.matches("^?[a-zA-Z_][a-zA-Z_0-9]*")) {
					return value;
				}
				return String.format("\"%s\"", value)
			}
			override toValue(String value, INode node) throws ValueConverterException {
				if (value == null) {
					throw new ValueConverterException("null value", node, null)
				}
				if (value.matches("\\\".*\\\"")) {
					return value.subSequence(1, value.length - 1).toString;
				}
				return value
			}
		}
	}
	
	// -------------------------------------------------------------------------------------------
	// ---------------------------------------- Helper -------------------------------------------
	// -------------------------------------------------------------------------------------------
	
	/**
	 * Checks for color tag ([#color|hexcode]) in an arrow
	 * @param string The string which contains a color tag.
	 * @return Color as hex or word or null if it contains syntax errors.
	 */
	def String checkForColorTag(String string){
		if(!string.contains("[")){
			return null;
		}
		// contains color tag
		
		var buffer = string.split("\\[").get(0);
		if(buffer.contains("]")){
			return buffer = buffer.split("\\]").get(0);
		}
		// Parsing Error
		return null;
	}
	
	/**
	 * Removes color tag from an association.
	 * @param string The string which contains a color tag.
	 * @return The given string without color information or null if the string does contain syntax errors.
	 */
	def String removeColorTag(String string){
		var stringBuffer = new StringBuffer();
		if(!string.contains("[")){
			return string;
		}
		// contains color tag
		stringBuffer.append(string.split("\\[").get(0));
		if(string.contains("]")){
			return stringBuffer.append(string.split("\\]").get(1)).toString();
		}
		// Parsing Error
		return null;
	}
	
	/**
	 * Removes the orientation information (= u | d | l | r) of an arrow.
	 * @param string The string which contains orientation information.
	 * @return The given string without orientation information.
	 */
	 def String removeOrientationInformation(String string){
		val orientations = #["l","r","u","d"]
		var buffer = "";
		for(String orientation : orientations){
			if(string.contains(orientation)){
				if (buffer.matches("")){
					buffer = orientation
				}else{
					// More than one orientation information
					return null
				}
			}
		}
		// Nothing to do
		if(buffer.matches("")){
			return string
		}
		return removeSubstring(string, buffer)
	 }
	 
	 /**
	  * Removes a substring from a string.
	  * Conditions: only one occurence of substring, first character can't be the end of the string.
	  * @param string The string containing the substring to remove.
	  * @param sub The substring to remove.
	  * @return The new string or null if conditions are violated.
	  */
	def String removeSubstring(String string, String sub){
		var stringBuffer = new StringBuffer()
		if(string.contains(sub)){
			if(string.indexOf(sub) != string.length()-1){
				stringBuffer.append(string.replaceFirst(sub,""))
				if(!stringBuffer.toString().contains(sub)){
					return stringBuffer.toString()
				}
				return null
			}
			return null
		}
		return string
	}
	
	/**
	 * Edits the length of an association arrow to a standardized length (=1).
	 * @param string The association arrow as a string.
	 */
	def String fixLength(String string){
		// Check for arrow style
		var type = '-'
		if(string.contains(".")){
			if(string.contains("-")) {
				// We don't allow mixed styles
				// (does not make any sense, although it's not a syntax error in PlantUML)
				return null;
			}
			type = '.';
		}
		// search for first occurence of character
		val firstIndex = string.indexOf(type);
		// replace this character with special character
		var buffer = string.substring(0,firstIndex) + "$" + string.substring(firstIndex+1);
		// delete all other occurences of character
		buffer = buffer.replace(type.toString(), "");
		// replace special character with style information
		buffer = buffer.replace("$",type);
		// insert single character for style information
		return buffer;
	}	
}
