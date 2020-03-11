import java.sql.SQLException;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import java.sql.SQLData;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import oracle.sql.STRUCT;
import oracle.jpub.runtime.MutableStruct;
public class AccountT implements SQLData{
    public static final String _SQL_NAME = "SYSTEM.ACCOUNT_T";
    public static final int _SQL_TYPECODE = OracleTypes.STRUCT;

    private String m_bNum;
    private String m_aNum;
    private String m_cNum;
    private Float m_balance;

    public AccountT(){ }

    public AccountT(String bNum, String aNum, String cNum, Float balance) {
        m_bNum=bNum;
        m_aNum=aNum;
        m_cNum=cNum;
        m_balance=balance;
    }

    @Override
    public String getSQLTypeName() throws SQLException {
        return _SQL_NAME;
    }

    @Override
    public void readSQL(SQLInput stream, String typeName) throws SQLException {
        setBNum(stream.readString());
        setANum(stream.readString());
        setCNum(stream.readString());
        setBalance(new Float(stream.readFloat()));
    }

    @Override
    public void writeSQL(SQLOutput stream) throws SQLException {
        stream.writeString(getBNum());
        stream.writeString(getANum());
        stream.writeString(getCNum());
        stream.writeFloat(getBalance());
    }

    public void setBNum(String bNum){
        m_bNum=bNum;
    }

    public String getBNum(){
        return m_bNum;
    }

    public void setANum(String aNum){
        m_aNum=aNum;
    }

    public String getANum(){
        return m_aNum;
    }

    public void setCNum(String cNum){
        m_cNum=cNum;
    }

    public String getCNum(){
        return m_cNum;
    }

    public void setBalance(Float balance){
        m_balance=balance;
    }

    public float getBalance(){
        return m_balance;
    }
}
