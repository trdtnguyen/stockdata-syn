# Synthetic Stock Data
Generate stock data with pre-defined patterns.

## Patterns
Common patterns in stock market ([more](https://optionalpha.com/13-stock-chart-patterns-that-you-cant-afford-to-forget-10585.html)):

* **Double Top (dt)**: The M-like pattern with three peaks on top.
* **Head and shouders top (hs)**: Look like the double top but the midle peak is higher than the left and the right
* **Inverse head and shouders**: Similar to the head and shouders pattern but inversed.


## Generating stock data
A `point (x, y)` is defined as a 2-D coordination point with x is the date and y is the price of the stock at that date.
A `pattern` is a sequence of `point` that follow a common sharp in the stock market. See [Patterns](#patterns) for more information.

Given an input N seed points follow a specific pattern, we generate the stock data from seed points by combining following steps:
* Step 1: Unniform scaling seed points to get uniform points with parameter K. The (N-1)*K generated points and N seed poins become the input for the next step.
* Step 2: Time-warping scaling uniform points to get time-warping points. The number of points is unchanged.
* Step 3: Pick M important points from (N-1)*K + N points (after the time-warping scaling). Important points are point that take an important role in forming the pattern that if remove one important point than the sharp is changed. In order words, unimportant points are points that not effect to forming the parttern.

### Uniform scaling
* **Purpose**: Increase more samples for a given pattern that keep the pattern unchanged.

* **Algorithm**: For a given N seed points, generate K points eventually between each pair of points (P1, P2). As the result, (N - 1) * K generated points and the original N seed points form the same pattern as previous seed points.

### Time-Warping Scaling
Step 1: For a given point P2(x2, y2), its two adjacent points P1(x1, y1), P3(x3, y3) and a predefined parameter a, we compute two middle points P1-2(u, v) and P2-3(z, t) as:

```
u = x2 - a * (x2 - x1)
v = y2 - a * (y2 - y1)
z = x2 + a * (x3 - x2)
t = y2 + a * (y3 - y2)
```

Step 2: Then generate two new points A(u', v'), B(z', t') from P1-2 and P2-3 as:

```
u' = x1 + rand() * (u - x1)
v' = y1 + rand() * (v - y1)
z' = u + rand() * (x3 - z)
t' = t + rand() * (y3 - t)
```

Step 3: Randomly choose between point A and point B to become the point C
Step 4: Replace point P2 by point C

### Extracting important points
Picking M important points to form the pattern. Usually, M > N

Put all (N-1) * K points after time-warping scaling into a binary search tree ordered by the important degree of a point. The important degree of a point tell how significant it effect to the pattern. We compute the important degree by using vertical distance from a given point to the start point and the end point of the sequence.

