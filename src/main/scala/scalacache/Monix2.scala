package scalacache

import monix.eval.Task
import monix.execution.{Cancelable, Scheduler}

import scala.util.control.NonFatal
import scala.util.{Failure, Success}

object Monix2 {

  private def AsyncForMonixTask(implicit scheduler: Scheduler): Async[Task] = new Async[Task] {

    override def pure[A](a: A): Task[A] = Task.pure(a)

    override def map[A, B](fa: Task[A])(f: A => B): Task[B] = fa.map(f)

    override def flatMap[A, B](fa: Task[A])(f: A => Task[B]): Task[B] = fa.flatMap(f)

    override def raiseError[A](t: Throwable): Task[A] = Task.raiseError(t)

    override def delay[A](thunk: => A): Task[A] = Task.delay(thunk)

    override def suspend[A](thunk: => Task[A]): Task[A] = Task.suspend(thunk)

    override def handleNonFatal[A](fa: => Task[A])(f: Throwable => A): Task[A] = fa.onErrorHandle {
      case NonFatal(e) => f(e)
    }

    override def async[A](register: (Either[Throwable, A] => Unit) => Unit) =
      Task.async[A] { (_, cb) =>
        register(eta => cb(eta.fold(Failure(_), Success(_))))
        Cancelable.empty
      }
  }

  object modes {
    implicit def mode(implicit scheduler: Scheduler): Mode[Task] = new Mode[Task] {
      val M: Async[Task] = AsyncForMonixTask
    }
  }

}
