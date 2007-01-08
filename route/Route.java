package route;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
		 * 長さ
		 */
		double length;

		/**
		 * 辺の種類
		 */
		@SuppressWarnings("unused")
		Category category;

		/**
		 * コンストラクタです。
		 * @param first 頂点1
		 * @param last 頂点2
		 * @param path 辺
		 * @param length 長さ
		 * @param category 辺の種類
		 */
		public Edge(final String first, final String last, final Shape path, final double length,
				final Category category) {
			this.first = first;
			this.last = last;
			this.path = path;
			this.length = length;
			this.category = category;
		}

		@Override
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

		@Override
		public String toString() {
			return this.node + "(" + this.value + ")";
		}
	}

	/**
	 * カテゴリを指定して速度を取得するためのインターフェイスです。
	 * @author zenjiro
	 * @since 6.1.0
	 */
	public interface Speed {
		/**
		 * カテゴリを指定して速度を取得します。
		 * @param category カテゴリ
		 * @return 速度[m/s]
		 */
		public double get(final Category category);

		/**
		 * @return 乗物の表記
		 */
		public String getVehicle();
	}

	/**
	 * インスタンス
	 */
	private static Route instance;

	/**
	 * 高速道路優先探索の速度
	 */
	public static final Speed HIGHWAY_SPEED = new Speed() {
		public double get(final Category category) {
			switch (category) {
			case ROAD_HIGHWAY:
				return 80 * 1000 / 3600;
			case ROAD_KOKUDO:
				return 50 * 1000 / 3600;
			case ROAD_KENDO:
			case ROAD_CHIHODO:
				return 45 * 1000 / 3600;
			case ROAD_MAJOR:
				return 40 * 1000 / 3600;
			case ROAD_OTHER:
			default:
				return 25 * 1000 / 3600;
			}
		}

		public String getVehicle() {
			return "車";
		}
	};

	/**
	 * 一般道優先探索の速度
	 */
	public static final Speed NORMAL_SPEED = new Speed() {
		public double get(final Category category) {
			if (category == Category.ROAD_HIGHWAY) {
				return 1;
			} else {
				return Route.HIGHWAY_SPEED.get(category);
			}
		}

		public String getVehicle() {
			return "車";
		}
	};

	/**
	 * 自転車の速度
	 */
	public static final Speed BIKE_SPEED = new Speed() {
		public double get(final Category category) {
			switch (category) {
			case ROAD_HIGHWAY:
				return 1;
			case ROAD_KOKUDO:
			case ROAD_KENDO:
			case ROAD_CHIHODO:
				return 18 * 1000 / 3600;
			case ROAD_MAJOR:
				return 16 * 1000 / 3600;
			case ROAD_OTHER:
			default:
				return 12 * 1000 / 3600;
			}
		}

		public String getVehicle() {
			return "自転車";
		}
	};

	/**
	 * 歩行者の速度
	 */
	public static final Speed WALK_SPEED = new Speed() {
		public double get(final Category category) {
			return 4.8 * 1000 / 3600;
		}

		public String getVehicle() {
			return "徒歩";
		}
	};

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
	 * 速度
	 */
	private Speed speed;

	/**
	 * ルートの文字列
	 */
	private String caption;

	/**
	 * 文字列の描画位置（仮想座標）
	 */
	private Point2D captionLocation;

	/**
	 * シングルトン用のコンストラクタです。
	 */
	private Route() {
		this.graph = new ConcurrentHashMap<String, Collection<Edge>>();
		this.route = new ArrayList<Shape>();
		this.points = new ArrayList<String>();
		this.cachedPoints = new ArrayList<Point2D>();
		this.speed = Route.HIGHWAY_SPEED;
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
			final float[] coords = new float[6];
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
		if (first.equals(last)) {
			return;
		}
		double length = 0;
		final PathIterator iterator = path.getPathIterator(new AffineTransform());
		Point2D lastPoint = null;
		while (!iterator.isDone()) {
			final float[] coords = new float[6];
			final int type = iterator.currentSegment(coords);
			final Point2D point = new Point2D.Float(coords[0], coords[1]);
			if (type == PathIterator.SEG_LINETO) {
				length += point.distance(lastPoint);
			}
			lastPoint = point;
			iterator.next();
		}
		final Edge edge = new Edge(first, last, path, length, category);
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
			if (this.points.contains(nearestNode)) {
				return;
			}
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
			while (this.removeGhostPoint()) {
			}
			this.cachedPoints.clear();
		}
	}

	/**
	 * 最短経路を求め、フィールドに記憶します。
	 */
	public void calcRoute() {
		this.route.clear();
		this.distance = 0;
		this.time = 0;
		String start = null;
		for (final String string : this.points) {
			if (start != null) {
				this.route.addAll(this.calcRoute(start, string));
			}
			start = string;
		}
		final String distance;
		if (this.distance + .5 < 1000) {
			distance = (int) (this.distance + .5) + "m";
		} else if (this.distance / 1000 + .05 < 10) {
			distance = new Formatter().format("%.1fkm", this.distance / 1000).toString();
		} else {
			distance = (int) (this.distance / 1000 + .5) + "km";
		}
		final String time;
		if (this.time / 60 < 60) {
			time = (int) (this.time / 60 + .5) + "分";
		} else {
			if ((int) (this.time / 60 % 60 + .5) == 0) {
				time = (int) (this.time / 60 / 60) + "時間";
			} else if ((int) (this.time / 60 % 60 + .5) == 30) {
				time = (int) (this.time / 60 / 60) + "時間半";
			} else {
				time = (int) (this.time / 60 / 60) + "時間" + (int) (this.time / 60 % 60 + .5) + "分";
			}
		}
		this.caption = distance + "（" + this.speed.getVehicle() + "で" + time + "）";
	}

	/**
	 * 求められた経路の長さ[m]
	 */
	private double distance;

	/**
	 * 求められた経路の時間[s]
	 */
	private double time;

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
				double distance = 0;
				double time = 0;
				while (parents.containsKey(node2)) {
					final Edge edge = parents.get(node2);
					ret.add(edge.path);
					distance += edge.length;
					time += edge.length / this.speed.get(edge.category);
					node2 = (edge.first.equals(node2)) ? edge.last : edge.first;
				}
				this.distance += distance;
				this.time += time;
				break;
			}
			doneNodes.add(node.node);
			if (!this.graph.containsKey(node.node)) {
				continue;
			}
			for (final Edge edge : this.graph.get(node.node)) {
				doneEdges.add(edge);
				for (final String node2 : new String[] { edge.first, edge.last }) {
					if (!doneNodes.contains(node2)) {
						if (nodes.containsKey(node2) && queue.contains(nodes.get(node2))) {
							final Node node3 = nodes.get(node2);
							if (node3.value > node.value + edge.length / this.speed.get(edge.category)) {
								node3.value = node.value + edge.length / this.speed.get(edge.category);
								queue.remove(node3);
								queue.add(node3);
								parents.put(node2, edge);
								//								System.out.println("removed and added " + node3);
							}
						} else {
							final Node node3 = new Node(node2, node.value + edge.length / this.speed.get(edge.category));
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
		this.distance = 0;
		this.time = 0;
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
	 * 地点をスキャンし、グラフに存在しないものがあれば1つ削除します。
	 * @return 削除したかどうか
	 */
	private boolean removeGhostPoint() {
		for (int i = 0; i < this.points.size(); i++) {
			if (!this.graph.containsKey(this.points.get(i))) {
				this.points.remove(i);
				return true;
			}
		}
		return false;
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
	 * @param speed 速度
	 */
	public void setSpeed(final Speed speed) {
		this.speed = speed;
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
	 * @return 総経路長
	 */
	public double getDistance() {
		return this.distance;
	}

	/**
	 * @return 総移動時間
	 */
	public double getTime() {
		return this.time;
	}

	/**
	 * @return 文字列の描画位置（仮想座標）
	 */
	public Point2D getCaptionLocation() {
		return this.captionLocation;
	}

	/**
	 * @param captionLocation 文字列の描画位置（仮想座標）
	 */
	public void setCaptionLocation(final Point2D captionLocation) {
		this.captionLocation = captionLocation;
	}

	/**
	 * @return ルートの文字列
	 */
	public String getCaption() {
		return this.caption;
	}

}
