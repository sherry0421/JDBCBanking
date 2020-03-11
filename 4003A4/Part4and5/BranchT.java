import java.sql.SQLException;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import java.sql.SQLData;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import oracle.sql.STRUCT;
import oracle.jpub.runtime.MutableStruct;
public class BranchT implements SQLData{
    public static final String _SQL_NAME = "SYSTEM.BRANCH_T";
    public static final int _SQL_TYPECODE = OracleTypes.STRUCT;

    private String m_bNum;
    private String m_address;

    public BranchT(){ }

    public BranchT(String bNum, String address){
        setBNum(bNum);
        setAddress(address);
    }

    @Override
    public String getSQLTypeName() throws SQLException {
        return _SQL_NAME;
    }

    @Override
    public void readSQL(SQLInput stream, String typeName) throws SQLException {
        setBNum(stream.readString());
        setAddress(stream.readString());
    }

    @Override
    public void writeSQL(SQLOutput stream) throws SQLException {
        stream.writeString(getBNum());
        stream.writeString(getAddress());
    }

    public void setBNum(String bNum){
        m_bNum=bNum;
    }

    public String getBNum(){
        return m_bNum;
    }

    public void setAddress(String address){
        m_address=address;
    }

    public String getAddress(){
        return m_address;
    }
}
