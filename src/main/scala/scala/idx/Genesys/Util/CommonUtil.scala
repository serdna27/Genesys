package scala.idx.Genesys.Util

object CommonUtil {

  val dbDriverMapping=Map("sql-server"->"net.sourceforge.jtds.jdbc.Driver")
  .+("postgres"->"org.postgresql.Driver").+("mysql"->"com.mysql.jdbc.Driver").+("sqlite"->"org.sqlite.JDBC")
  
}