import SwiftUI
import shared_client

@main
struct iOSApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self)
    var appDelegate: AppDelegate
    
    private var rootHolder: RootHolder { appDelegate.getRootHolder() }
    
    var body: some Scene {
        WindowGroup {
            ContentView(component: rootHolder.root)
                .ignoresSafeArea(.all)
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.didBecomeActiveNotification)) { _ in
                    LifecycleRegistryExtKt.resume(rootHolder.lifecycle)
                }
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.willResignActiveNotification)) { _ in
                    LifecycleRegistryExtKt.pause(rootHolder.lifecycle)
                }
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.didEnterBackgroundNotification)) { _ in
                    LifecycleRegistryExtKt.stop(rootHolder.lifecycle)
                }
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.willTerminateNotification)) { _ in
                    LifecycleRegistryExtKt.destroy(rootHolder.lifecycle)
                }
        }
    }
}

private let STATE_KEY: String = "mini-games-saved-state"

class AppDelegate: NSObject, UIApplicationDelegate {
    
    private var rootHolder: RootHolder?
    
    func application(_ application: UIApplication, shouldSaveSecureApplicationState coder: NSCoder) -> Bool {
        StateKeeperUtilsKt.save(coder: coder, state: rootHolder!.stateKeeper.save())
        return true
    }
    
    func application(_ application: UIApplication, shouldRestoreSecureApplicationState coder: NSCoder) -> Bool {
        do {
            let savedState = StateKeeperUtilsKt.restore(coder: coder)
            rootHolder = RootHolder(savedState: savedState)
            return true
        } catch {
            return false
        }
    }
    
    fileprivate func getRootHolder() -> RootHolder {
        if (rootHolder == nil) {
            rootHolder = RootHolder(savedState: nil)
        }
        return rootHolder!
    }
}

private class RootHolder {
    let lifecycle: LifecycleRegistry
    let stateKeeper: StateKeeperDispatcher
    let root: MiniGamesAppComponent
    
    init(savedState: SerializableContainer?) {
        lifecycle = LifecycleRegistryKt.LifecycleRegistry()
        stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: savedState)
        
        root = MiniGamesAppComponentImpl(
            appContext: MiniGamesAppComponentContext(),
            componentContext: DefaultComponentContext(
                lifecycle: lifecycle,
                stateKeeper: stateKeeper,
                instanceKeeper: nil,
                backHandler: nil
            )
        )
        
        LifecycleRegistryExtKt.create(lifecycle)
    }
}
