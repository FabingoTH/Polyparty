#version 330 core
// --- constants
#define GAMMA 2.2f
#define INV_GAMMA (1.0f / 2.2f)
#define PI 3.14159265359f
#define INV_PI (1.0f / PI)

#define MAX_POINT_LIGHTS 10
#define MAX_SPOT_LIGHTS 10

#define CEL_LEVELS 3.0f

// --- Input from vertex shader
in struct VertexData
{
    vec2 textureCoordinate;
    vec3 normal;
// Vectors needed for lighting calculations. Must not be normalized in the vertex shader.
    vec3 toCamera;
    vec3 toPointLight[MAX_POINT_LIGHTS];
    vec3 toSpotLight[MAX_SPOT_LIGHTS];
} vertexData;

// --- Materials
// Material textures
uniform sampler2D materialDiff;
uniform sampler2D materialSpec;
uniform sampler2D materialEmit;
// Material shininess parameter
uniform float materialShininess;
// Multiply with (grayscale) emmissive texture to get "glow" in different colors.
uniform vec3 shadingColor;

// --- Lights
struct PointLight
{
    vec3 Color;
    vec3 Position;
};
struct SpotLight
{
    vec3 Color;
    vec3 Position;
    vec2 Cone;
    vec3 Direction;
};
// Fixed-size uniform arrays, but with a runtime-configurable number of lights
uniform PointLight pointLight[MAX_POINT_LIGHTS];
uniform int numPointLights;
uniform SpotLight spotLight[MAX_SPOT_LIGHTS];
uniform int numSpotLights;

// --- Fragment shader output
out vec4 color;

// --- Calculates the amount of light that arrives at the shaded point from a point light
vec3 getPointLightIntensity(vec3 color, vec3 toLightVector)
{
    // distance to the light source is the length of the toLightVector
    float d = length(toLightVector);
    // incident light is light color multiplied with inverse-square-law attenuation
    return color * (1.0f / (d * d));
}

// --- Calculates the amount of light that arrives at the shaded point from a spot light
vec3 getSpotLightIntensity(vec3 color, vec3 toLightVector, vec3 lightdir, vec2 cone)
{
    // distance to the light source is the length of the toLightVector
    float d = length(toLightVector);
    // cosine of the angle between the spot light direction and the vector from spot light to shaded point
    float cosfpos = dot(lightdir, normalize(-toLightVector));
    // attenuation factor is 1 inside the inner cone, 0 outside the outer cone and a linear ramp in between
    float att = clamp((cosfpos - cos(cone.y)) / (cos(cone.x) - cos(cone.y)), 0.0f, 1.0f);
    // incident light is light color multiplied with inverse-square-law attenuation and the cone attenuation factor
    return color * att * (1.0f / (d * d));
}

// --- converts from linear-RGB to gamma-RGB
vec3 gammaCorrect(vec3 clinear)
{
    return pow(clinear, vec3(INV_GAMMA));
}

// --- converts from gamma-RGB to linear-RGB
vec3 invGammaCorrect(vec3 cgamma)
{
    return pow(cgamma, vec3(GAMMA));
}

vec3 shade(vec3 unitNormal, vec3 unitToLight, vec3 unitToCamera, vec3 diffuse, vec3 specc, float shininess)
{
    vec3 halfwayVector = normalize(unitToCamera + unitToLight);
    float brightness = max(dot(unitNormal, unitToLight), 0.5f);
    float celLevel = floor(brightness * CEL_LEVELS);
    brightness = celLevel / CEL_LEVELS;
    float HdotN = max(dot(halfwayVector, unitNormal), 0.0f);
    float test = pow(HdotN, shininess);
    float celLevelSpec = floor(test * CEL_LEVELS);
    test = celLevelSpec / CEL_LEVELS;

    return diffuse * brightness + specc * celLevelSpec;
}

void main(){
    // Sample material properties from the textures and convert to linear-RGB
    vec3 diffColor = invGammaCorrect(texture(materialDiff, vertexData.textureCoordinate).rgb);
    vec3 specColor = invGammaCorrect(texture(materialSpec, vertexData.textureCoordinate).rgb);
    vec3 emitColor = invGammaCorrect(texture(materialEmit, vertexData.textureCoordinate).rgb);

    // Gather and normalize vectors needed for lighting calculations
    vec3 unitSurfaceNormal = normalize(vertexData.normal);
    vec3 unitToCamera = normalize(vertexData.toCamera);

    // Initialize the color accumulator with light due to self-emission.
    vec3 emitTerm = emitColor * shadingColor;
    vec3 finalColor = emitTerm;

    // Process all point lights
    for (int i = 0; i < numPointLights; i++) {
        vec3 unitToLight = normalize(vertexData.toPointLight[i]);
        vec3 pointLightShade = shade(unitSurfaceNormal, unitToLight, unitToCamera, diffColor, specColor, materialShininess*2);
        // Get the incoming light intensity including inverse-square-law attenuation
        vec3 intPointLight = getPointLightIntensity(pointLight[i].Color, vertexData.toPointLight[i]);
        // Multiply BRDF with incoming light intensity to get total color contribution for point light i
        finalColor += pointLightShade * intPointLight;
    }

    // Process all spot lights
    for (int i = 0; i < numSpotLights; i++) {
        vec3 unitToLight = normalize(vertexData.toSpotLight[i]);
        vec3 spotLightShade = shade(unitSurfaceNormal, unitToLight, unitToCamera, diffColor, specColor, materialShininess*2);
        // Get the incoming light intensity including inverse-square-law attenuation and cone attenuation
        vec3 intSpotLight = getSpotLightIntensity(spotLight[i].Color, vertexData.toSpotLight[i], spotLight[i].Direction, spotLight[i].Cone);
        // Multiply BRDF with incoming light intensity to get total color contribution for spot light i
        finalColor += spotLightShade * intSpotLight;
    }

    // Convert linear-RGB to gamma-RGB to account for the monitor's non-linearity and store the final fragment color.
    color = vec4(gammaCorrect(finalColor), 1.0f);
}