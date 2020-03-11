import java.sql.SQLException;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import java.sql.SQLData;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import oracle.sql.STRUCT;
import oracle.jpub.runtime.MutableStruct;
public class CustomerT implements  SQLData{
    public static final String _SQL_NAME = "SYSTEM.CUSTOMER_T";
    public static final int _SQL_TYPECODE = OracleTypes.STRUCT;

    private String m_cNum;
    private String m_name;
    private Integer m_status;

    public CustomerT(){ }

    public CustomerT(String cNum, String name, Integer status) {
        m_cNum=cNum;
        m_name=name;
        m_status=status;

    }

    @Override
    public String getSQLTypeName() throws SQLException {
        return _SQL_NAME;
    }

    @Override
    public void readSQL(SQLInput stream, String typeName) throws SQLException {
        setCNum(stream.readString());
        setName(stream.readString());
        setStatus(new Integer(stream.readInt()));
        if(stream.wasNull()) setStatus(null);
    }

    @Override
    public void writeSQL(SQLOutput stream) throws SQLException {
        stream.writeString(getCNum());
        stream.writeString(getName());
        if(getStatus()==null)
            stream.writeBigDecimal(null);
        else
            stream.writeInt(getStatus().intValue());
    }

    public void setCNum(String cNum){
        m_cNum=cNum;
    }

    public String getCNum(){
        return m_cNum;
    }

    public void setName(String name){
        m_name=name;
    }

    public String getName(){
        return m_name;
    }

    public void setStatus(Integer status) {
        m_status = status;
    }

    public Integer getStatus() {
        return m_status;
    }
}
