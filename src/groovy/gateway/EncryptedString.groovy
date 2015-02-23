package gateway;
import java.sql.*

import org.hibernate.*
import org.hibernate.usertype.UserType

import de.unibayreuth.bayeos.connection.SimpleEncrypter;


 
class EncryptedString implements UserType {

	static final String KEY = "0123456789012345678901234567890123456789"
 
    int[] sqlTypes() { [Types.VARCHAR] as int[] }
    Class returnedClass() { String }
    boolean equals(x, y) { x == y }
    int hashCode(x) { x.hashCode() }
    Object deepCopy(value) { value }
    boolean isMutable() { false }
    Serializable disassemble(value) { value }
    def assemble(Serializable cached, owner) { cached }
    def replace(original, target, owner) { original }
 
    Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {
        String str = resultSet.getString(names[0])
        str ? decrypt(str) : null
    }
 
    void nullSafeSet(PreparedStatement statement, Object value, int index) {
        statement.setString(index, value ? encrypt(value) : null)
    }
    private String encrypt(String plaintext) {
		SimpleEncrypter.encrypt(KEY, plaintext)
    }
 
    private String decrypt(String ciphertext) {
		SimpleEncrypter.decrypt(KEY, ciphertext)
    }
}