package org.example;

public class LineAnalyzer {
    private String relationshipType = "";
    private Point intersectionPoint = null;
    private boolean hasIntersection = false;

    public void analyze(Line line1, Line line2) {
        // Отримуємо координати точок
        float x1 = line1.getStart().getX();
        float y1 = line1.getStart().getY();
        float x2 = line1.getEnd().getX();
        float y2 = line1.getEnd().getY();

        float x3 = line2.getStart().getX();
        float y3 = line2.getStart().getY();
        float x4 = line2.getEnd().getX();
        float y4 = line2.getEnd().getY();

        // Перевіряємо випадок, коли відрізки співпадають
        if (pointsEqual(x1, y1, x3, y3) && pointsEqual(x2, y2, x4, y4) ||
                pointsEqual(x1, y1, x4, y4) && pointsEqual(x2, y2, x3, y3)) {
            relationshipType = "Відрізки співпадають повністю";
            hasIntersection = true;
            intersectionPoint = new Point(x1, y1);
            return;
        }

        // Перевіряємо випадок накладання кінцевих точок
        if (pointsEqual(x1, y1, x3, y3) || pointsEqual(x1, y1, x4, y4)) {
            relationshipType = "Відрізки мають спільну точку (початок першого відрізка)";
            hasIntersection = true;
            intersectionPoint = new Point(x1, y1);
            return;
        }

        if (pointsEqual(x2, y2, x3, y3) || pointsEqual(x2, y2, x4, y4)) {
            relationshipType = "Відрізки мають спільну точку (кінець першого відрізка)";
            hasIntersection = true;
            intersectionPoint = new Point(x2, y2);
            return;
        }

        // Коефіцієнти першої лінії (Ax + By + C = 0)
        float A1 = y1 - y2;
        float B1 = x2 - x1;
        float C1 = x1 * y2 - y1 * x2;

        // Коефіцієнти другої лінії
        float A2 = y3 - y4;
        float B2 = x4 - x3;
        float C2 = x3 * y4 - y3 * x4;

        // Розрахунок значень S1, S2, S3, S4
        float s1 = x1 * (y3 - y4) + y1 * (x4 - x3) + (x3 * y4 - y3 * x4);
        float s2 = x2 * (y3 - y4) + y2 * (x4 - x3) + (x3 * y4 - y3 * x4);
        float s3 = x3 * (y1 - y2) + y3 * (x2 - x1) + (x1 * y2 - y1 * x2);
        float s4 = x4 * (y1 - y2) + y4 * (x2 - x1) + (x1 * y2 - y1 * x2);

        // Умова перетину відрізків: знаки S1,S2 мають бути різними, а також знаки S3,S4 мають бути різними
        boolean intersect = (s1 * s2 <= 0) && (s3 * s4 <= 0);

        if (intersect) {
            // Обчислюємо детермінант для розв'язання системи рівнянь
            float d = A1 * B2 - A2 * B1;

            if (!almostEqual(d, 0)) {
                float intersectX = (B1 * C2 - B2 * C1) / d;
                float intersectY = (A2 * C1 - A1 * C2) / d;

                relationshipType = "Відрізки перетинаються в одній точці";
                hasIntersection = true;
                intersectionPoint = new Point(intersectX, intersectY);

            }
        } else {
            // Відрізки не перетинаються

            // Перевіряємо, чи паралельні прямі
            // Умова паралельності: A1/A2 = B1/B2
            if (almostEqual(A1 * B2, A2 * B1)) {
                relationshipType = "Паралельні відрізки";
                hasIntersection = false;
            } else {
                // Лежать на прямих, що перетинаються, але самі не перетинаються
                relationshipType = "Відрізки лежать на прямих, що перетинаються, але не мають спільних точок";

                // Обчислюємо точку перетину прямих
                float d = A1 * B2 - A2 * B1;
                if (!almostEqual(d, 0)) {
                    float intersectX = (B1 * C2 - B2 * C1) / d;
                    float intersectY = (A2 * C1 - A1 * C2) / d;

                    intersectionPoint = new Point(intersectX, intersectY);
                }
            }

            hasIntersection = false;
        }
    }

    private boolean isPointOnSegment(float px, float py, float x1, float y1, float x2, float y2) {
        // Перевіряє, чи лежить точка (px, py) на відрізку (x1,y1)-(x2,y2)
        float d1 = distance(px, py, x1, y1);
        float d2 = distance(px, py, x2, y2);
        float lineLen = distance(x1, y1, x2, y2);

        return almostEqual(d1 + d2, lineLen);
    }

    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    private boolean pointsEqual(float x1, float y1, float x2, float y2) {
        return almostEqual(x1, x2) && almostEqual(y1, y2);
    }

    private boolean almostEqual(float a, float b) {
        // Порівняння з урахуванням похибки обчислень
        return Math.abs(a - b) < 0.0001f;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public Point getIntersectionPoint() {
        return intersectionPoint;
    }

    public boolean hasIntersection() {
        return hasIntersection;
    }

    // Додаткова інформація для відображення
    public String getDebugInfo(Line line1, Line line2) {
        float x1 = line1.getStart().getX();
        float y1 = line1.getStart().getY();
        float x2 = line1.getEnd().getX();
        float y2 = line1.getEnd().getY();

        float x3 = line2.getStart().getX();
        float y3 = line2.getStart().getY();
        float x4 = line2.getEnd().getX();
        float y4 = line2.getEnd().getY();

        // S1, S2, S3, S4
        float s1 = x1 * (y3 - y4) + y1 * (x4 - x3) + (x3 * y4 - y3 * x4);
        float s2 = x2 * (y3 - y4) + y2 * (x4 - x3) + (x3 * y4 - y3 * x4);
        float s3 = x3 * (y1 - y2) + y3 * (x2 - x1) + (x1 * y2 - y1 * x2);
        float s4 = x4 * (y1 - y2) + y4 * (x2 - x1) + (x1 * y2 - y1 * x2);

        return String.format(
                "S1*S2=%s, S3*S4=%s\nS1=%.4f, S2=%.4f, S3=%.4f, S4=%.4f",
                (s1 * s2 <= 0) ? "<0" : ">0",
                (s3 * s4 <= 0) ? "<0" : ">0",
                s1, s2, s3, s4
        );
    }
}