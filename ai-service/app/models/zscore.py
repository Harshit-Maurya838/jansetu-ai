"""WHO Child Growth Standards z-scores via the LMS method.

z = ((X / M) ** L - 1) / (L * S), with the WHO restricted-tail adjustment for weight-based
indicators when |z| > 3 (WHO Anthro technical report, 2006).

TODO(team): add the official WHO LMS tables (weight-for-age, length/height-for-age,
weight-for-length, weight-for-height; boys and girls) under app/models/who_lms/ and a loader.
Download them from the WHO Child Growth Standards site; do not hand-type values.
"""

import math


def lms_zscore(x: float, l: float, m: float, s: float, restricted_tail: bool = False) -> float:
    if x <= 0 or m <= 0 or s <= 0:
        raise ValueError("measurement, M and S must be positive")
    z = math.log(x / m) / s if l == 0 else ((x / m) ** l - 1) / (l * s)
    if not restricted_tail or abs(z) <= 3:
        return z

    def sd(k: float) -> float:  # value at k standard deviations
        return m * (1 + l * s * k) ** (1 / l)

    if z > 3:
        sd3, sd23 = sd(3), sd(3) - sd(2)
        return 3 + (x - sd3) / sd23
    sd3n, sd23n = sd(-3), sd(-2) - sd(-3)
    return -3 + (x - sd3n) / sd23n


def classify_whz(whz: float | None, oedema: bool) -> str:
    """Wasting classification (WHO/IAP): SEVERE < -3, MODERATE -3..-2, NORMAL >= -2. Oedema => SEVERE."""
    if oedema:
        return "SEVERE"
    if whz is None:
        return "NOT_ASSESSED"
    if whz < -3:
        return "SEVERE"
    if whz < -2:
        return "MODERATE"
    return "NORMAL"
