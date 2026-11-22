import math

import pytest

from app.models.zscore import classify_whz, lms_zscore


def test_median_is_zero():
    assert lms_zscore(9.0, 0.1, 9.0, 0.1) == pytest.approx(0.0)


def test_known_value():
    # X = M * (1 + L*S*z)^(1/L) inverted for z = -2
    l, m, s = -0.3521, 9.4789, 0.10958
    x = m * (1 + l * s * -2) ** (1 / l)
    assert lms_zscore(x, l, m, s) == pytest.approx(-2.0, abs=1e-9)


def test_l_zero_uses_log():
    assert lms_zscore(math.e, 0, 1, 1) == pytest.approx(1.0)


def test_restricted_tail_beyond_minus_3():
    l, m, s = 0.5, 10.0, 0.1
    sd = lambda k: m * (1 + l * s * k) ** (1 / l)  # noqa: E731
    x = sd(-3) - (sd(-2) - sd(-3))  # one "SD23" below -3SD
    assert lms_zscore(x, l, m, s, restricted_tail=True) == pytest.approx(-4.0)


def test_rejects_non_positive():
    with pytest.raises(ValueError):
        lms_zscore(0, 0.1, 9, 0.1)


@pytest.mark.parametrize(
    "whz,oedema,expected",
    [
        (-3.5, False, "SEVERE"),
        (-2.5, False, "MODERATE"),
        (-1.0, False, "NORMAL"),
        (0.5, True, "SEVERE"),
        (None, False, "NOT_ASSESSED"),
    ],
)
def test_classify_whz(whz, oedema, expected):
    assert classify_whz(whz, oedema) == expected
