package honor

/**
 * @author ponkotuy
 * Date: 15/05/05.
 */
object HasUsers extends HonorCategory {
  override def category: Int = 17

  override def comment: String = "提督人数突破記念"

  override def approved(memberId: Long): List[String] = List("提督500人突破")
}
