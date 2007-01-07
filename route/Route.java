package route;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * 最短経路探索を行うクラスです。
 * @author zenjiro
 * @since 5.03
 * 2007/01/01
 */
public class Route {

	/**
	 * 辺の種類を表す列挙型です。
	 * @author zenjiro
	 * @since 5.03
	 * 2007/01/01
	 */
	public enum Category {
		/**
		 * 高速道路
		 */
		ROAD_HIGHWAY,
		/**
		 * 国道
		 */
		ROAD_KOKUDO,
		/**
		 * 県道
		 */
		ROAD_KENDO,
		/**
		 * 主要地方道
		 */
		ROAD_CHIHODO,
		/**
		 * 名前のある道路
		 */
		ROAD_MAJOR,
		/**
		 * 一般道
		 */
		ROAD_OTHER,
		/**
		 * JR新幹線
		 */
		RAILWAY_SHINKANSEN,
		/**
		 * JR在来線
		 */
		RAILWAY_JR,
		/**
		 * 私鉄
		 */
		RAILWAY_OTHER,
		/**
		 * 徒歩
		 */
		RAILWAY_WALK,
		/**
		 * 不明な辺
		 */
		UNKNOWN,
	}

	/**
	 * 辺をカプセル化するクラスです。
	 * @author zenjiro
	 * @since 5.03
	 */
	private static class Edge {
		/**
		 * 頂点1
		 */
		String first;

		/**
		 * 頂点2
		 */
		String last;

		/**
		 * 辺
		 */
		Shape path;

		/**
		 * 辺の種類
		 */
		@SuppressWarnings("unused")
		private Category category;

		/**
		 * コンストラクタです。
		 * @param first 頂点1
		 * @param last 頂点2
		 * @param path 辺
		 * @param category 辺の種類
		 */
		public Edge(final String first, final String last, final Shape path, final Category category) {
			this.first = first;
			this.last = last;
			this.path = path;
			this.category = category;
		}

		public String toString() {
			return this.first + "--" + this.last;
		}
	}

	/**
	 * 頂点と値をカプセル化する比較可能なクラスです。
	 * @author zenjiro
	 * @since 5.03
	 */
	private static class Node implements Comparable<Node> {
		/**
		 * 頂点
		 */
		String node;

		/**
		 * 値
		 */
		double value;

		/**
		 * コンストラクタです。
		 * @param node 頂点
		 * @param value 値
		 */
		public Node(final String node, final double value) {
			this.node = node;
			this.value = value;
		}

		public int compareTo(final Node other) {
			return this.value < other.value ? -1 : (this.value > other.value ? 1 : 0);
		}

		public String toString() {
			return this.node + "(" + this.value + ")";
		}
	}

	/**
	 * インスタンス
	 */
	private static Route instance;

	/**
	 * インスタンスを取得します。
	 * @return インスタンス
	 */
	public static Route getInstance() {
		if (Route.instance == null) {
			Route.instance = new Route();
		}
		return Route.instance;
	}

	/**
	 * @param string 文字列表現
	 * @return 点
	 */
	public Point2D toPoint(final String string) {
		final String[] items = string.split("_");
		return new Point2D.Double(Double.parseDouble(items[0]), Double.parseDouble(items[1]));
	}

	/**
	 * @param point 点
	 * @return 文字列表現
	 */
	public String toString(final Point2D point) {
		return (int) point.getX() + "_" + (int) point.getY();
	}

	/**
	 * 頂点をキー、辺の一覧を値とするグラフ
	 */
	private Map<String, Collection<Edge>> graph;

	/**
	 * 経由地の一覧
	 */
	private List<String> points;

	/**
	 * 最短経路
	 */
	private List<Shape> route;

	/**
	 * キャッシュされた経由地の一覧
	 */
	private List<Point2D> cachedPoints;

	/**
	 * シングルトン用のコンストラクタです。
	 */
	private Route() {
		this.graph = new HashMap<String, Collection<Edge>>();
		this.route = new ArrayList<Shape>();
		this.points = new ArrayList<String>();
		this.cachedPoints = new ArrayList<Point2D>();
	}

	/**
	 * 経路探索のための辺を1本追加します。
	 * @param path 辺
	 * @param category 辺の種類
	 */
	public void add(final Shape path, final Category category) {
		Point2D first = null;
		Point2D last = null;
		final PathIterator iterator = path.getPathIterator(new AffineTransform());
		while (!iterator.isDone()) {
			float[] coords = new float[6];
			final int type = iterator.currentSegment(coords);
			if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
				if (first == null) {
					first = new Point2D.Double(coords[0], coords[1]);
				} else {
					last = new Point2D.Double(coords[0], coords[1]);
				}
			}
			iterator.next();
		}
		this.add(this.toString(first), this.toString(last), path, category);
	}

	/**
	 * 経路探索のための辺を1本追加します。
	 * @param first 頂点1
	 * @param last 頂点2
	 * @param path 辺
	 * @param category 辺の種類
	 */
	public void add(final String first, final String last, final Shape path, final Category category) {
		final Edge edge = new Edge(first, last, path, category);
		for (final String node : new String[] { first, last }) {
			if (!this.graph.containsKey(node)) {
				this.graph.put(node, new HashSet<Edge>());
			}
			if (!this.graph.get(node).contains(edge)) {
				this.graph.get(node).add(edge);
			}
		}
	}

	/**
	 * 経由地を追加します。
	 * @param point 点
	 */
	public void addPoint(final Point2D point) {
		final String nearestNode = this.getNearestNode(point);
		if (nearestNode != null) {
			if (this.points.isEmpty()) {
				this.points.add(nearestNode);
			} else {
				final Point2D start = this.toPoint(this.points.get(0));
				final Point2D goal = this.toPoint(this.points.get(this.points.size() - 1));
				if (point.distanceSq(start) < point.distanceSq(goal)) {
					this.points.add(0, nearestNode);
				} else {
					this.points.add(nearestNode);
				}
			}
			this.cachedPoints.clear();
		}
	}

	/**
	 * 最短経路を求めます。
	 * @param start 始点
	 * @param goal 終点
	 * @return 最短経路
	 */
	private List<Shape> calcRoute(final String start, final String goal) {
		final List<Shape> ret = new ArrayList<Shape>();
		if (start == null || goal == null || !this.graph.containsKey(start) || !this.graph.containsKey(goal)) {
			return ret;
		}
		final Map<String, Edge> parents = new HashMap<String, Edge>();
		final Map<String, Node> nodes = new HashMap<String, Node>();
		final Set<Edge> doneEdges = new HashSet<Edge>();
		final Set<String> doneNodes = new HashSet<String>();
		final PriorityQueue<Node> queue = new PriorityQueue<Node>();
		queue.add(new Node(start, 0));
		while (!queue.isEmpty()) {
			//			System.out.printf("queue = %s, doneNodes = %s, doneEdges = %s\n", queue, doneNodes, doneEdges);
			final Node node = queue.poll();
			//			System.out.println("polled " + node);
			if (node.node.equals(goal)) {
				String node2 = goal;
				while (parents.containsKey(node2)) {
					final Edge edge = parents.get(node2);
					ret.add(edge.path);
					node2 = (edge.first == node2) ? edge.last : edge.first;
				}
				break;
			}
			doneNodes.add(node.node);
			if (!this.graph.containsKey(node.node)) {
				continue;
			}
			for (final Edge edge : this.graph.get(node.node)) {
				doneEdges.add(edge);
				for (final String node2 : new String[] { edge.first, edge.last }) {
					// TODO とりあえず始点終点間の直線距離を長さとしてみる。
					final double length = this.toPoint(edge.first).distance(this.toPoint(edge.last));
					if (!doneNodes.contains(node2)) {
						if (nodes.containsKey(node2) && queue.contains(nodes.get(node2))) {
							final Node node3 = nodes.get(node2);
							if (node3.value > node.value + length) {
								node3.value = node.value + length;
								queue.remove(node3);
								queue.add(node3);
								parents.put(node2, edge);
								//								System.out.println("removed and added " + node3);
							}
						} else {
							final Node node3 = new Node(node2, node.value + length);
							nodes.put(node2, node3);
							queue.add(node3);
							parents.put(node2, edge);
							//							System.out.println("added " + node3);
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * 最短経路を求め、フィールドに記憶します。
	 */
	public void calcRoute() {
		this.route.clear();
		String start = null;
		for (final String string : this.points) {
			if (start != null) {
				this.route.addAll(calcRoute(start, string));
			}
			start = string;
		}
	}

	/**
	 * 経路探索のためのグラフをクリアします。
	 */
	public void clear() {
		this.graph.clear();
		this.cachedPoints.clear();
	}

	/**
	 * ルートと経由地をクリアします。
	 */
	public void clearRoute() {
		this.route.clear();
		this.points.clear();
		this.cachedPoints.clear();
	}

	/**
	 * @param point 点
	 * @return 最近傍ノード
	 */
	public String getNearestNode(final Point2D point) {
		String ret = null;
		double minDistance = Double.POSITIVE_INFINITY;
		for (final String node : this.graph.keySet()) {
			final Point2D point2 = this.toPoint(node);
			final double distance = point.distanceSq(point2);
			if (distance < minDistance) {
				ret = node;
				minDistance = distance;
			}
		}
		return ret;
	}

	/**
	 * 指定した点に最も近い経由地を取得します。経由地の移動用のものなので、ある程度近いものしか取得できません。
	 * @param point 点
	 * @param radius 経由地を求める最大の距離
	 * @return 指定した点に最も近い経由地
	 */
	public Point2D getNearestPoint(final Point2D point, final double radius) {
		double minDistance = Double.POSITIVE_INFINITY;
		Point2D ret = null;
		for (final Point2D point2 : this.getPoints()) {
			final double distance = point.distance(point2);
			if (distance < minDistance) {
				ret = point2;
				minDistance = distance;
			}
		}
		if (minDistance < radius) {
			return ret;
		} else {
			return null;
		}
	}

	/**
	 * 経由地の一覧を取得します。これは頻繁に呼び出されるので、キャッシュする必要があります。
	 * @return 経由地の一覧
	 */
	public List<Point2D> getPoints() {
		if (this.cachedPoints.isEmpty()) {
			for (final String string : this.points) {
				this.cachedPoints.add(this.toPoint(string));
			}
		}
		return this.cachedPoints;
	}

	/**
	 * @return 最短経路
	 */
	public List<Shape> getRoute() {
		if (this.route.isEmpty()) {
			this.calcRoute();
		}
		return this.route;
	}

	/**
	 * 経由地を挿入します。挿入される位置は自動的に決定されます。
	 * @param point 点
	 */
	public void insertPoint(final Point2D point) {
		int index = -1;
		double minDistance = Double.POSITIVE_INFINITY;
		for (int i = 1; i < this.points.size(); i++) {
			final Point2D point1 = this.toPoint(this.points.get(i - 1));
			final Point2D point2 = this.toPoint(this.points.get(i));
			final double distance = point.distance(point1) + point.distance(point2);
			if (distance < minDistance) {
				index = i;
				minDistance = distance;
			}
		}
		if (index != -1) {
			this.points.add(index, this.getNearestNode(point));
			this.cachedPoints.clear();
		}
	}

	/**
	 * 指定した点に最も近い経由地を削除します。ある程度近いものしか削除できません。
	 * @param point 点
	 * @param radius 削除する地点の最大距離
	 */
	public void removeNearestPoint(final Point2D point, final double radius) {
		final Point2D nearestPoint = this.getNearestPoint(point, radius);
		if (nearestPoint != null) {
			final String string = this.toString(nearestPoint);
			int index = -1;
			for (int i = 0; i < this.points.size(); i++) {
				final String string2 = this.points.get(i);
				if (string2.equals(string)) {
					index = i;
				}
			}
			if (index > -1) {
				this.points.remove(index);
				this.cachedPoints.clear();
			}
		}
	}

	/**
	 * 辺の種類毎に速度を設定します。
	 * @param category 辺の種類
	 * @param speed 速度[km/h]
	 */
	public void setSpeed(final Category category, final double speed) {
		// TODO 未実装
	}

}
