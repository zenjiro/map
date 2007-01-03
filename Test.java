import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import route.Route;
import route.Route.Category;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Shape a = new Line2D.Double(0, 0, 100, 0);
		final Shape b = new Line2D.Double(0, 0, 0, 100);
		final Shape c = new Line2D.Double(100, 0, 100, 100);
		final Shape d = new Line2D.Double(0, 100, 100, 100);
		final Shape e = new Line2D.Double(0, 0, 50, 50);
		final Shape f = new Line2D.Double(50, 50, 90, 90);
		final Shape g = new Line2D.Double(90, 90, 100, 100);
		Route.getInstance().add(a, Category.UNKNOWN);
		Route.getInstance().add(b, Category.UNKNOWN);
		Route.getInstance().add(c, Category.UNKNOWN);
		Route.getInstance().add(d, Category.UNKNOWN);
		Route.getInstance().add(e, Category.UNKNOWN);
		Route.getInstance().add(f, Category.UNKNOWN);
		Route.getInstance().add(g, Category.UNKNOWN);
		Route.getInstance().setStart(Route.getInstance().getNearestNode(new Point2D.Double(0, 0)));
		Route.getInstance().setGoal(Route.getInstance().getNearestNode(new Point2D.Double(100, 100)));
		System.out.println("route = " + Route.getInstance().getRoute());
	}

}
