import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.publish.PublishingExtension

val publishingProjects = setOf(":velocity-api", ":velocity-native", ":velocity-proxy")

fun configureRepository(project: Project) {
    val repositoryUrlProvider = project.providers.gradleProperty("MAVEN_URL")
    val repositoryUsernameProvider = project.providers.gradleProperty("MAVEN_USER").orElse("")
    val repositoryPasswordProvider = project.providers.gradleProperty("MAVEN_PASS").orElse("")

    project.extensions.configure<PublishingExtension> {
        val targetRepository: MavenArtifactRepository = repositories
            .withType<MavenArtifactRepository>()
            .findByName("velopub")
            ?: repositories.maven {
                name = "velopub"
            }
        targetRepository.setUrl(repositoryUrlProvider)
        val username = repositoryUsernameProvider.get()
        val password = repositoryPasswordProvider.get()
        if (username.isNotBlank() && password.isNotBlank()) {
            targetRepository.credentials(PasswordCredentials::class.java) {
                this.username = username
                this.password = password
            }
        }
    }

    project.tasks.named("publish") {
        setDependsOn(listOf("publishAllPublicationsToVelopubRepository"))
    }
}

gradle.beforeProject(Action<Project> {
    if (path in publishingProjects && !pluginManager.hasPlugin("velocity-publish")) {
        pluginManager.apply("velocity-publish")
    }
})

gradle.afterProject(Action<Project> {
    if (path in publishingProjects) {
        configureRepository(this)
    }
})
