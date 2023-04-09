package ml.dev.kotlin.minigames.shared.ui.component.bird

import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import ml.dev.kotlin.minigames.shared.ui.theme.Shapes

enum class BirdTheme(
        val bodyColor: Color,
        val bodyShape: Shape,
        val wingColor: Color,
        val nozzleColor: Color,
) {
    STORK(
            bodyColor = Color(0xffffffff),
            bodyShape = Shapes.medium,
            wingColor = Color(0xff000000),
            nozzleColor = Color(0xffff0000),
    ),
    SPARROW(
            bodyColor = Color(0xff793d00),
            bodyShape = CircleShape,
            wingColor = Color(0xff482700),
            nozzleColor = Color(0xffffc637)
    ),
    RED_PARROT(
            bodyColor = Color(0xfff80a00),
            bodyShape = AbsoluteCutCornerShape(6.dp),
            wingColor = Color(0xffbb0900),
            nozzleColor = Color(0xffffd737)
    ),
    GREEN_PARROT(
            bodyColor = Color(0xff09c700),
            bodyShape = AbsoluteCutCornerShape(6.dp),
            wingColor = Color(0xff068500),
            nozzleColor = Color(0xffffd737)
    ),
    RAVEN(
            bodyColor = Color(0xff464646),
            bodyShape = Shapes.medium,
            wingColor = Color(0xff000000),
            nozzleColor = Color(0xff2d2d2d)
    ),
    GOLDFINCH(
            bodyColor = Color(0xffffd737),
            bodyShape = CircleShape,
            wingColor = Color(0xff000000),
            nozzleColor = Color(0xffef7905)
    ),
}
