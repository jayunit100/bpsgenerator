import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.google.common.annotations.VisibleForTesting
import sparkapps.SparkApp1

/**
 * Created by apache on 7/20/14.
 */
class Tester {

  @org.junit.Test
  def test(){

    SparkApp1.main(Array("1"));

  }
}