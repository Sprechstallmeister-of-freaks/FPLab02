import domain.*
import logic.WarehouseLogic
import application.WarehouseService

object WarehouseTests:

  def runAll(): Unit =
    /*
       /\_/\
      ( o.o )   Кот-тестировщик.
       > ^ <    Следит, чтобы ничего не упало.
    */

    val inventory = Inventory(Map("book" -> 5, "pen" -> 10))
    val config = ShippingConfig(
      Map(PackageType.Small -> 100.0, PackageType.Medium -> 200.0, PackageType.Large -> 350.0),
      20.0,
      1000.0
    )

    // Тест 1: товара хватает — заказ собирается
    val order1 = Order(List(Item("book", 0.5, 300.0)), 0.5, 300.0)
    assert(WarehouseLogic.canAssemble(order1, inventory))
    println("PASS: Test 1 — enough stock, order assembled")

    // Тест 2: товара нет — заказ отклонён
    val order2 = Order(List(Item("laptop", 2.0, 50000.0)), 2.0, 50000.0)
    assert(!WarehouseLogic.canAssemble(order2, inventory))
    println("PASS: Test 2 — insufficient stock, order rejected")

    // Тест 3: тяжёлый заказ — крупная упаковка
    assert(WarehouseLogic.packageType(15.0) == PackageType.Large)
    println("PASS: Test 3 — heavy order, large package")

    // Тест 4: дорогой заказ — бесплатная доставка
    assert(WarehouseLogic.freeShipping(1500.0).read(config))
    println("PASS: Test 4 — expensive order, free shipping")

    // Тест 5: резервирование при достаточных остатках
    val order3 = Order(List(Item("book", 0.5, 300.0)), 0.5, 300.0)
    val initialState = WarehouseState(inventory, List.empty, List.empty)
    val (reserveResult, stateAfter) = WarehouseService.reserveItems(order3).run(initialState)
    assert(reserveResult.isRight)
    assert(stateAfter.inventory.stock("book") == 4)
    println("PASS: Test 5 — state transition, items reserved")

    // Тест 6: резервирование при нехватке — состояние не меняется
    val order4 = Order(List(Item("laptop", 2.0, 50000.0)), 2.0, 50000.0)
    val (reserveResult2, stateAfter2) = WarehouseService.reserveItems(order4).run(initialState)
    assert(reserveResult2.isLeft)
    assert(stateAfter2.inventory.stock == initialState.inventory.stock)
    println("PASS: Test 6 — state transition, reservation rejected")

    // Тест 7: полный цикл — заказ отправлен
    val (fullResult, _) = WarehouseService.processOrderFull(order3).run(initialState)
    assert(fullResult.isRight)
    println("PASS: Test 7 — full process, order shipped")

    // Тест 8: полный цикл — заказ отклонён
    val (fullResult2, _) = WarehouseService.processOrderFull(order4).run(initialState)
    assert(fullResult2.isLeft)
    println("PASS: Test 8 — full process, order rejected")

    println("\n=== ALL TESTS PASSED ===")